package site.neworld.objective.regionstorage

import net.minecraft.core.ChunkPos
import org.apache.logging.log4j.LogManager
import java.io.*
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.*

class RegionFile(file: Path, folder: Path, version: RegionFileVersion) : AutoCloseable {
    private val file: FileChannel
    private var externalFileDir: Path
    private val version: RegionFileVersion
    private val header: ByteBuffer
    private var offsets: IntBuffer
    private var timestamps: IntBuffer
    private val usedSectors: SectorBitmap // a sector is 4096 bytes, aka 4KB

    constructor(file: File, folder: File) : this(file.toPath(), folder.toPath(), RegionFileVersion.VERSION_DEFLATE)

    init {
        header = ByteBuffer.allocateDirect(8192)
        usedSectors = SectorBitmap()
        this.version = version
        require(Files.isDirectory(folder)) { "Expected directory, got " + folder.toAbsolutePath() }
        externalFileDir = folder
        offsets = header.asIntBuffer()
        offsets.limit(1024)
        header.position(4096)
        timestamps = header.asIntBuffer()
        this.file = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)
        usedSectors.force(0, 2)
        header.position(0)
        val headerSize = this.file.read(header, 0L)
        if (headerSize != -1) {
            if (headerSize != 8192) {
                LOGGER.warn("Region file {} has truncated header: {}", file, headerSize)
            }
            for (i in 0..1023) {
                val offset = offsets[i]
                if (offset != 0) {
                    val sectorNumber = getSectorNumber(offset)
                    val numSectors = getNumSectors(offset)
                    usedSectors.force(sectorNumber, numSectors)
                }
            }
        } else {
            writeHeader()
        }
    }

    @Synchronized
    fun read(pos: ChunkPos): ByteArray? {
        val offset = offsets[getOffsetIndex(pos)]
        return if (offset == 0) {
            null
        } else {
            val sectorNumber = getSectorNumber(offset)
            val numSectors = getNumSectors(offset)
            val chunkCapacity = numSectors * 4096
            val chunk = ByteBuffer.allocate(chunkCapacity)
            file.read(chunk, sectorNumber * 4096.toLong())
            chunk.flip()
            if (chunk.remaining() < 5) {
                throw IOException("Chunk ${pos} header is truncated: expected ${chunkCapacity} but read ${chunk.remaining()}")
            } else {
                val chunkSize = chunk.getInt()
                if (chunkSize == 0) {
                    throw IOException("Chunk ${pos} is allocated, but stream is missing")
                } else {
                    val chunkVersion = chunk.get()
                    val chunkActualSize = chunkSize - 1
                    if (isExternalStreamChunk(chunkVersion)) {
                        if (chunkActualSize != 0) {
                            LOGGER.warn("Chunk has both internal and external streams, read external stream")
                        }
                        val externalFile = getExternalChunkPath(pos)
                        if (Files.isRegularFile(externalFile)) {
                            readChunkStream(pos, getExternalChunkVersion(chunkVersion), Files.newInputStream(externalFile))
                        } else {
                            throw IOException("External chunk path ${externalFile} is not file")
                        }
                    } else if (chunkActualSize > chunk.remaining()) {
                        throw IOException("Chunk ${pos} stream is truncated: expected ${chunkActualSize} but read ${chunk.remaining()}")
                    } else if (chunkActualSize < 0) {
                        throw IOException("Declared size ${chunkSize} of chunk ${pos} is negative")
                    } else {
                        readChunkStream(pos, chunkVersion, ByteArrayInputStream(chunk.array(), chunk.position(), chunkActualSize))
                    }
                }
            }
        }
    }

    @Synchronized
    fun write(pos: ChunkPos, data: ByteArray) {
        val offsetIndex = getOffsetIndex(pos)
        val offset = offsets[offsetIndex]
        val oldSectorNumber = getSectorNumber(offset)
        val oldNumSectors = getNumSectors(offset)
        val chunkDataBuffer = object : ByteArrayOutputStream(8192) {
            fun toByteBuffer() = ByteBuffer.wrap(buf, 0, count)
        }.use {
            writeChunkStream(version.id.toByte(), it, data)
            it.toByteBuffer()
        }
        val chunkSize = 4 + 1 + chunkDataBuffer.remaining()
        var numSectors = sizeToSectors(chunkSize)
        val sectorNumber: Int
        val commitOp: () -> Unit
        if (numSectors >= 256) {
            val externalFile = getExternalChunkPath(pos)
            numSectors = 1
            sectorNumber = usedSectors.allocate(numSectors)
            commitOp = writeToExternalFile(externalFile, chunkDataBuffer)
            val stub = createExternalStub()
            file.write(stub, sectorNumber * 4096.toLong())
        } else {
            sectorNumber = usedSectors.allocate(numSectors)
            commitOp = { Files.deleteIfExists(getExternalChunkPath(pos)) }
            val streamBuffer = ByteBuffer.allocateDirect(chunkSize)
            streamBuffer.putInt(chunkSize)
            streamBuffer.put(version.id.toByte())
            streamBuffer.put(chunkDataBuffer)
            streamBuffer.flip()
            file.write(streamBuffer, sectorNumber * 4096.toLong())
        }
        val timestamp = (Instant.now().toEpochMilli() / 1000L).toInt()
        offsets.put(offsetIndex, packSectorOffset(sectorNumber, numSectors))
        timestamps.put(offsetIndex, timestamp)
        writeHeader()
        commitOp()
        if (oldSectorNumber != 0) {
            usedSectors.free(oldSectorNumber, oldNumSectors)
        }
    }

    @Synchronized
    override fun close() {
        try {
            padToFullSector()
        } finally {
            try {
                writeHeader()
            } finally {
                try {
                    file.force(true)
                } finally {
                    file.close()
                }
            }
        }
    }

    private fun readChunkStream(pos: ChunkPos, version: Byte, inputStream: InputStream): ByteArray {
        return if (RegionFileVersion.isValidVersion(version.toInt())) {
            val wrapper = RegionFileVersion.fromId(version.toInt())
            wrapper.wrap(inputStream).buffered().use { it.readAllBytes() }
        } else {
            throw IOException("Chunk ${pos} has invalid chunk stream version ${version}")
        }
    }

    private fun writeChunkStream(version: Byte, outputStream: OutputStream, data: ByteArray) {
        if (RegionFileVersion.isValidVersion(version.toInt())) {
            val wrapper = RegionFileVersion.fromId(version.toInt())
            wrapper.wrap(outputStream).buffered().use { it.write(data) }
        } else {
            throw IOException("Invalid chunk stream version ${version}")
        }
    }

    private fun getExternalChunkPath(pos: ChunkPos): Path {
        val mccFile = "c." + pos.x + "." + pos.z + ".mcc"
        return externalFileDir.resolve(mccFile)
    }

    private fun createExternalStub(): ByteBuffer {
        val stub = ByteBuffer.allocate(5)
        stub.putInt(1)
        stub.put((version.id or 128).toByte())
        stub.flip()
        return stub
    }

    private fun writeToExternalFile(externalFile: Path, data: ByteBuffer): () -> Unit {
        val temp = Files.createTempFile(externalFileDir, "tmp", null)
        FileChannel.open(temp, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { it.write(data) }
        return { Files.move(temp, externalFile, StandardCopyOption.REPLACE_EXISTING) }
    }

    private fun writeHeader() {
        header.position(0)
        file.write(header, 0L)
    }

    private fun padToFullSector() {
        val size = file.size().toInt()
        val properSize = sizeToSectors(size) * 4096
        if (size != properSize) {
            val paddingBuffer = PADDING_BUFFER.duplicate()
            paddingBuffer.position(0)
            file.write(paddingBuffer, properSize - 1.toLong())
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        private val PADDING_BUFFER = ByteBuffer.allocateDirect(1)
        private fun isExternalStreamChunk(version: Byte): Boolean {
            return version.toInt() and 128 != 0
        }

        private fun getExternalChunkVersion(version: Byte): Byte {
            return (version.toInt() and 127).toByte()
        }

        private fun getNumSectors(offset: Int): Int {
            return offset and 255
        }

        private fun getSectorNumber(offset: Int): Int {
            return offset shr 8
        }

        private fun packSectorOffset(sectorNumber: Int, numSectors: Int): Int {
            return (sectorNumber shl 8) or numSectors
        }

        private fun sizeToSectors(size: Int): Int {
            return (size - 1) / 4096 + 1
        }

        private fun getOffsetIndex(pos: ChunkPos): Int {
            return pos.regionLocalX + pos.regionLocalZ * 32
        }
    }
}

enum class RegionFileVersion(val id: Int,
                             private val inputWrapper: (InputStream) -> InputStream,
                             private val outputWrapper: (OutputStream) -> OutputStream) {
    VERSION_GZIP(1, { java.util.zip.GZIPInputStream(it) }, { java.util.zip.GZIPOutputStream(it) }),
    VERSION_DEFLATE(2, { java.util.zip.InflaterInputStream(it) }, { java.util.zip.DeflaterOutputStream(it) }),
    VERSION_NONE(3, { it }, { it });

    companion object {

        private val VERSIONS = values().associateBy { it.id }

        fun fromId(id: Int) = VERSIONS[id] ?: throw IllegalArgumentException("invalid version id")

        fun isValidVersion(id: Int) = VERSIONS.containsKey(id)

    }

    fun wrap(inputStream: InputStream): InputStream = inputWrapper(inputStream)

    fun wrap(outputStream: OutputStream): OutputStream = outputWrapper(outputStream)

}

private class SectorBitmap {
    private val used = BitSet()

    fun force(pos: Int, size: Int) {
        used.set(pos, pos + size)
    }

    fun free(pos: Int, size: Int) {
        used.clear(pos, pos + size)
    }

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
