package site.neworld.objective.data

import kotlinx.coroutines.runBlocking
import site.neworld.objective.utils.ChunkPos
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class RegionStorage(private val file: SectorFile, private val oversize: OversizeStorage, private val version: Compression) : AutoCloseable {
    constructor(file: SectorFile, oversize: OversizeStorage) : this(file, oversize, Compression.DEFLATE)

    private fun warp(buffer: ByteBuffer) =
            ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit() - buffer.position())

    suspend fun read(pos: ChunkPos): ByteArray? {
        val data = file.readObject(pos.toIndex()) ?: return null
        val (method, dataSize) = readHeader(data, pos)
        if (isExternal(method)) {
            if (dataSize != 0) Warning.DUAL_STREAM.issue(pos, dataSize)
            readStream(getMethodExternal(method), warp(oversize.get(pos)))
        }
        return when {
            dataSize < 0 -> Error.INVALID_STREAM_SIZE.raise(pos, dataSize)
            dataSize > data.remaining() -> Error.TRUNCATED_STREAM.raise(pos, dataSize, data.remaining())
            else -> readStream(method, warp(data.limit(data.position() + dataSize)))
        }
    }

    suspend fun write(pos: ChunkPos, data: ByteArray) {
        val dataBuffer = writeStreamAsBuffer(data)
        val size = dataBuffer.remaining()
        val index = pos.toIndex()
        if (size + 5 >= sizeThreshold) {
            val commit = oversize.set(pos, dataBuffer)
            file.writeObject(index, constructObject(0, version.id or 128) {}) // external stub
            commit()
        } else {
            file.writeObject(index, constructObject(size, version.id) { it.put(dataBuffer) })
            oversize.clear(pos)
        }
    }

    override fun close() = runBlocking { file.closeAsync() }

    private fun readHeader(data: ByteBuffer, pos: ChunkPos): Pair<Byte, Int> {
        if (data.remaining() < 5) Error.TRUNCATED_STREAM_HEADER.raise(pos, data.remaining())
        val size = data.int.apply { if (this == 0) Error.VOID_STREAM.raise(pos) }
        return Pair(data.get(), size - 1)
    }

    private fun writeStreamAsBuffer(data: ByteArray) =
            object : ByteArrayOutputStream(8192) {
                fun toByteBuffer() = ByteBuffer.wrap(buf, 0, count)
            }.also { writeStream(version.id.toByte(), it, data) }.toByteBuffer()

    private fun readStream(version: Byte, inputStream: InputStream) =
            Compression.fromId(version.toInt()).wrap(inputStream).use { it.readAllBytes() }

    private fun writeStream(version: Byte, outputStream: OutputStream, data: ByteArray) =
            Compression.fromId(version.toInt()).wrap(outputStream).use { it.write(data) }

    private inline fun constructObject(size: Int, version: Int, prepare: (ByteBuffer) -> Unit) =
            ByteBuffer.allocate(size + 5).putInt(size).put(version.toByte()).also(prepare).flip()

    companion object {
        private const val sizeThreshold = 4096 * 256
        private fun isExternal(version: Byte) = version.toInt() and 128 != 0
        private fun getMethodExternal(version: Byte) = (version.toInt() and 127).toByte()
        private fun ChunkPos.toIndex() = regionLocalX or (regionLocalZ shl 5)
    }
}
