package site.neworld.objective.data

import site.neworld.objective.utils.ChunkPos
import java.nio.ByteBuffer

class RegionStorage(private val file: SectorFile, private val oversize: OversizeStorage) {
    suspend fun read(pos: ChunkPos): Pair<Byte, ByteBuffer>? {
        val data = file.readObject(pos.toIndex()) ?: return null
        val (method, dataSize) = readHeader(data, pos)
        if (isExternal(method)) {
            if (dataSize != 0) Warning.DUAL_STREAM.issue(pos, dataSize)
            return Pair(getMethodExternal(method), oversize.get(pos))
        }
        return when {
            dataSize < 0 -> Error.INVALID_STREAM_SIZE.raise(pos, dataSize)
            dataSize > data.remaining() -> Error.TRUNCATED_STREAM.raise(pos, dataSize, data.remaining())
            else -> Pair(method, data)
        }
    }

    suspend fun write(pos: ChunkPos, data: ByteBuffer, version: Int) {
        val size = data.remaining()
        val index = pos.toIndex()
        if (size + 5 >= sizeThreshold) {
            val commit = oversize.set(pos, data)
            file.writeObject(index, constructObject(0, version or 128) {}) // external stub
            commit()
        } else {
            file.writeObject(index, constructObject(size, version) { it.put(data) })
            oversize.clear(pos)
        }
    }

    suspend fun closeAsync() = file.closeAsync()

    private fun readHeader(data: ByteBuffer, pos: ChunkPos): Pair<Byte, Int> {
        if (data.remaining() < 5) Error.TRUNCATED_STREAM_HEADER.raise(pos, data.remaining())
        val size = data.int.apply { if (this == 0) Error.VOID_STREAM.raise(pos) }
        return Pair(data.get(), size - 1)
    }

    private inline fun constructObject(size: Int, version: Int, prepare: (ByteBuffer) -> Unit) =
            ByteBuffer.allocate(size + 5).putInt(size).put(version.toByte()).also(prepare).flip()

    companion object {
        private const val sizeThreshold = 4096 * 256
        private fun isExternal(version: Byte) = version.toInt() and 128 != 0
        private fun getMethodExternal(version: Byte) = (version.toInt() and 127).toByte()
        private fun ChunkPos.toIndex() = regionLocalX or (regionLocalZ shl 5)
    }
}
