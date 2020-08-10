package site.neworld.objective.data

import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.aRead
import site.neworld.objective.utils.aWrite
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

@Suppress("BlockingMethodInNonBlockingContext")
class LargeEntryStorage(private val directory: Path) {
    private fun createPath(pos: ChunkPos) = directory.resolve("c.${pos.x}.${pos.z}.mcc")

    suspend fun set(pos: ChunkPos, data: ByteBuffer): () -> Unit {
        val temp = Files.createTempFile(directory, "tmp", null)
        AsynchronousFileChannel.open(temp, StandardOpenOption.WRITE).use { it.aWrite(data, 0) }
        return { Files.move(temp, createPath(pos), StandardCopyOption.REPLACE_EXISTING) }
    }

    suspend fun get(pos: ChunkPos): ByteBuffer =
            AsynchronousFileChannel.open(createPath(pos), StandardOpenOption.READ).use {
                val size = it.size().toInt()
                ByteBuffer.allocate(size).limit(size).apply { it.aRead(this, 0L) }
            }

    fun clear(pos: ChunkPos) = Files.deleteIfExists(createPath(pos))
}

class Region(private val file: SectorFile, private val oversized: LargeEntryStorage, private val version: Compression) : AutoCloseable {
    constructor(file: SectorFile, oversized: LargeEntryStorage) : this(file, oversized, Compression.DEFLATE)

    private fun warp(buffer: ByteBuffer) =
            ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit() - buffer.position())

    suspend fun read(pos: ChunkPos): ByteArray? {
        val data = file.readObject(pos.toIndex()) ?: return null
        if (data.remaining() < 5) throw IOException("Chunk header is truncated: read ${data.remaining()}")
        val chunkSize = data.getInt()
        if (chunkSize == 0) throw IOException("Chunk is allocated, but stream is missing")
        val chunkVersion = data.get()
        val chunkActualSize = chunkSize - 1
        if (isExternal(chunkVersion)) {
            if (chunkActualSize != 0) LOGGER.warn("Chunk has both internal and external streams, read external stream")
            readStream(getExternalVersion(chunkVersion), warp(oversized.get(pos)))
        }
        return when {
            chunkActualSize > data.remaining() -> {
                throw IOException("Chunk stream is truncated: expected ${chunkActualSize} but read ${data.remaining()}")
            }
            chunkActualSize < 0 -> throw IOException("Declared size ${chunkSize} of chunk is negative")
            else -> readStream(chunkVersion, warp(data.limit(data.position() + chunkActualSize)))
        }
    }

    suspend fun write(pos: ChunkPos, data: ByteArray) {
        val chunkDataBuffer = object : ByteArrayOutputStream(8192) {
            fun toByteBuffer() = ByteBuffer.wrap(buf, 0, count)
        }.also { writeStream(version.id.toByte(), it, data) }.toByteBuffer()
        val chunkSize = chunkDataBuffer.remaining()
        val index = pos.toIndex()
        if (sizeToSectors(chunkSize + 5) >= 256) {
            val commit = oversized.set(pos, chunkDataBuffer)
            file.writeObject(index, constructObject(0, version.id or 128) {}) // external stub
            commit()
        } else {
            file.writeObject(index, constructObject(chunkSize, version.id) { it.put(chunkDataBuffer) })
            oversized.clear(pos)
        }
    }

    override fun close() = runBlocking { file.closeAsync() }

    private fun readStream(version: Byte, inputStream: InputStream) =
            Compression.fromId(version.toInt()).wrap(inputStream).use { it.readAllBytes() }

    private fun writeStream(version: Byte, outputStream: OutputStream, data: ByteArray) =
            Compression.fromId(version.toInt()).wrap(outputStream).use { it.write(data) }

    private inline fun constructObject(size: Int, version: Int, prepare: (ByteBuffer)->Unit) =
            ByteBuffer.allocate(size + 5).putInt(size).put(version.toByte()).also(prepare).flip()

    companion object {
        private val LOGGER = LogManager.getLogger()

        private fun isExternal(version: Byte) = version.toInt() and 128 != 0

        private fun getExternalVersion(version: Byte) = (version.toInt() and 127).toByte()

        private fun sizeToSectors(size: Int) = (size - 1) / 4096 + 1

        private fun ChunkPos.toIndex() = regionLocalX or (regionLocalZ shl 5)
    }
}

