package site.neworld.objective.data

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.ExceptionAggregator
import site.neworld.objective.utils.aRead
import site.neworld.objective.utils.aWrite
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.*

private inline class SectorBitmap(private val used: BitSet = BitSet()) {
    fun force(pos: Int, size: Int) = used.set(pos, pos + size)

    fun free(pos: Int, size: Int) = used.clear(pos, pos + size)

    fun allocate(size: Int): Int {
        var ptr = 0
        while (true) {
            val ptrBegin = used.nextClearBit(ptr)
            val ptrEnd = used.nextSetBit(ptrBegin)
            if (ptrEnd == -1 || ptrEnd - ptrBegin >= size) {
                force(ptrBegin, size)
                return ptrBegin
            }
            ptr = ptrEnd
        }
    }
}

private fun clone(original: ByteBuffer): ByteBuffer {
    val clone = ByteBuffer.allocate(original.capacity())
    original.rewind() //copy from the beginning
    clone.put(original)
    original.rewind()
    clone.flip()
    return clone
}

private fun getNumSectors(packed: Int) = packed and 255

private fun getSectorNumber(packed: Int) = packed shr 8

private fun packSectorAlloc(start: Int, count: Int) = (start shl 8) or count

private fun computeSectors(size: Int) = (size - 1) / 4096 + 1

class SectorFile private constructor(file: Path) {
    private val file = AsynchronousFileChannel.open(
            file,
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE
    )
    private val header = ByteBuffer.allocateDirect(8192)
    private var allocs: IntBuffer
    private var lockTable = Int2IntOpenHashMap()
    private var timestamps: IntBuffer
    private val bitmap = SectorBitmap() // a sector is 4096 bytes, aka 4KB
    private var headerFileVer = 0L
    private var headerMemVer = 0L

    init {
        allocs = header.position(0).asIntBuffer().limit(1024)
        timestamps = header.position(4096).asIntBuffer().limit(1024)
        header.position(0)
        bitmap.force(0, 2) // the first two sectors are used by the header
    }

    private suspend fun asyncInit() {
        val headerSize = file.aRead(header, 0L)
        if (headerSize == -1) return writeHeader()
        if (headerSize < 8192) Error.BULK_HEADER_TRUNCATED.raise(ChunkPos.ZERO, headerSize)
        for (i in 0..1023) {
            val alloc = allocs[i]
            if (alloc != 0) bitmap.force(getSectorNumber(alloc), getNumSectors(alloc))
        }
    }

    private suspend fun writeHeader() {
        header.position(0)
        file.aWrite(header, 0L)
    }

    private fun allocate(sectors: Int) = synchronized(bitmap) { bitmap.allocate(sectors) }

    private fun free(start: Int, sectors: Int) = synchronized(bitmap) { bitmap.free(start, sectors) }

    private fun refAlloc(index: Int) = synchronized(allocs) {
        val res = allocs[index]
        if (res != 0) lockTable.addTo(res, 1)
        res
    }

    private fun relAlloc(alloc: Int) = synchronized(allocs) {
        if (lockTable.addTo(alloc, -1) == freeMagic + 1) {
            free(getSectorNumber(alloc), getNumSectors(alloc))
            lockTable.remove(alloc)
        }
    }

    private fun swapAlloc(index: Int, newAlloc: Int) = synchronized(allocs) {
        val thisAlloc = allocs[index]
        if (lockTable.containsKey(thisAlloc)) lockTable.addTo(thisAlloc, freeMagic)
        else free(getSectorNumber(thisAlloc), getNumSectors(thisAlloc))
        allocs.put(index, newAlloc)
        val timestamp = (Instant.now().toEpochMilli() / 1000L).toInt()
        timestamps.put(index, timestamp)
        synchronized(header) { ++headerMemVer }
    }

    suspend fun readObject(index: Int): ByteBuffer? {
        val alloc = refAlloc(index)
        if (alloc == 0) return null
        val chunk = ByteBuffer.allocate(getNumSectors(alloc) * 4096)
        val ret = file.aRead(chunk, getSectorNumber(alloc) * 4096L)
        relAlloc(alloc)
        if (ret == -1) Error.BULK_TRUNCATED_FILE.raise(ChunkPos.ZERO, index)
        return chunk.flip()
    }

    suspend fun writeObject(index: Int, bytes: ByteBuffer) {
        val sectorCount = computeSectors(bytes.remaining())
        val sectorStart = allocate(sectorCount)
        file.aWrite(bytes, sectorStart * 4096L)
        val thisVer = swapAlloc(index, packSectorAlloc(sectorStart, sectorCount))
        GlobalScope.launch(HEADER_WRITER) { writeHeader(thisVer) }
    }

    private suspend fun writeHeader(onVer: Long) {
        val snapshot: ByteBuffer
        synchronized(header) {
            if (onVer <= headerFileVer) return
            headerFileVer = headerMemVer
            snapshot = clone(header)
        }
        file.aWrite(snapshot, 0L)
    }

    suspend fun closeAsync() = ExceptionAggregator().run<Unit> {
        runCaptured { padToFullSector() }
        runCaptured { writeHeader() }
        runCaptured { file.force(true) }
        runCaptured { file.close() }
    }

    private suspend fun padToFullSector() {
        val size = file.size().toInt()
        val properSize = computeSectors(size) * 4096
        if (size != properSize) {
            val paddingBuffer = PADDING_BUFFER.duplicate()
            paddingBuffer.position(0)
            file.aWrite(paddingBuffer, properSize - 1.toLong())
        }
    }

    companion object {
        private val PADDING_BUFFER = ByteBuffer.allocateDirect(1)
        private val HEADER_WRITER = Concurrency.newSynchronizedCoroutineContext()

        suspend fun open(file: Path): SectorFile {
            val result = SectorFile(file)
            result.asyncInit()
            return result
        }

        // 0x10000 is not an absolutely safe value,
        // but nobody sanely using a minecraft server shall have this many concurrent reads
        private const val freeMagic = 0x10000
    }
}