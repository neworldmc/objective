package site.neworld.objective.data

import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.aRead
import site.neworld.objective.utils.aWrite
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

@Suppress("BlockingMethodInNonBlockingContext")
class OversizeStorage(private val directory: Path) {
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