package site.neworld.objective.io

import kotlinx.coroutines.runBlocking
import net.minecraft.core.ChunkPos
import org.apache.logging.log4j.LogManager
import site.neworld.objective.utils.aRead
import site.neworld.objective.utils.aWrite
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class RegionFile(private val file: FixedFATFile, folder: Path, private val version: RegionFileVersion) : AutoCloseable {
    private var externalFileDir: Path = folder

    constructor(file: FixedFATFile, folder: File) : this(file, folder.toPath(), RegionFileVersion.VERSION_DEFLATE)

    init { require(Files.isDirectory(folder)) { "Expected directory, got " + folder.toAbsolutePath() } }

    private fun inputStreamBufferWarp(buffer: ByteBuffer): ByteArrayInputStream {
        return ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit() - buffer.position())
    }

    suspend fun read(pos: ChunkPos): ByteArray? {
        val data = file.readObject(getOffsetIndex(pos)) ?: return null
        if (data.remaining() < 5) throw IOException("Chunk header is truncated: read ${data.remaining()}")
        val chunkSize = data.getInt()
        if (chunkSize == 0) throw IOException("Chunk is allocated, but stream is missing")
        val chunkVersion = data.get()
        val chunkActualSize = chunkSize - 1
        if (isExternalStreamChunk(chunkVersion)) {
            if (chunkActualSize != 0) LOGGER.warn("Chunk has both internal and external streams, read external stream")
            val externalFile = getExternalChunkPath(pos)
            return if (Files.isRegularFile(externalFile)) {
                readChunkStream(getExternalChunkVersion(chunkVersion),
                        inputStreamBufferWarp(readFromExternalFile(externalFile))
                )
            } else {
                throw IOException("External chunk path ${externalFile} is not file")
            }
        }
        return when {
            chunkActualSize > data.remaining() -> {
                throw IOException("Chunk stream is truncated: expected ${chunkActualSize} but read ${data.remaining()}")
            }
            chunkActualSize < 0 -> throw IOException("Declared size ${chunkSize} of chunk is negative")
            else -> {
                readChunkStream(chunkVersion, inputStreamBufferWarp(data.limit(data.position() + chunkActualSize)))
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun write(pos: ChunkPos, data: ByteArray) {
        val chunkDataBuffer = object : ByteArrayOutputStream(8192) {
            fun toByteBuffer() = ByteBuffer.wrap(buf, 0, count)
        }.use {
            writeChunkStream(version.id.toByte(), it, data)
            it.toByteBuffer()
        }
        val chunkSize = 4 + 1 + chunkDataBuffer.remaining()
        val externalFile = getExternalChunkPath(pos)
        val index = getOffsetIndex(pos)
        if (sizeToSectors(chunkSize) >= 256) {
            val commit = writeToExternalFile(externalFile, chunkDataBuffer)
            file.writeObject(index, createExternalStub())
            commit()
        } else {
            file.writeObject(index, ByteBuffer.allocateDirect(chunkSize)
                    .putInt(chunkSize).put(version.id.toByte()).put(chunkDataBuffer).flip())
            Files.deleteIfExists(externalFile)
        }
    }

    override fun close() = runBlocking { file.closeAsync() }

    private fun readChunkStream(version: Byte, inputStream: InputStream): ByteArray {
        return if (RegionFileVersion.isValidVersion(version.toInt())) {
            val wrapper = RegionFileVersion.fromId(version.toInt())
            wrapper.wrap(inputStream).buffered().use { it.readAllBytes() }
        } else {
            throw IOException("Chunk has invalid chunk stream version ${version}")
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

    private fun getExternalChunkPath(pos: ChunkPos) = externalFileDir.resolve("c.${pos.x}.${pos.z}.mcc")

    private fun createExternalStub() = ByteBuffer.allocate(5).putInt(1).put((version.id or 128).toByte()).flip()

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun writeToExternalFile(externalFile: Path, data: ByteBuffer): () -> Unit {
        val temp = Files.createTempFile(externalFileDir, "tmp", null)
        AsynchronousFileChannel.open(temp, StandardOpenOption.WRITE).use { it.aWrite(data, 0) }
        return { Files.move(temp, externalFile, StandardCopyOption.REPLACE_EXISTING) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun readFromExternalFile(externalFile: Path): ByteBuffer =
        AsynchronousFileChannel.open(externalFile, StandardOpenOption.READ).use {
            val size = it.size().toInt()
            val buffer = ByteBuffer.allocate(size).limit(size)
            it.aRead(buffer, 0L)
            buffer
        }

    companion object {
        private val LOGGER = LogManager.getLogger()

        private fun isExternalStreamChunk(version: Byte) = version.toInt() and 128 != 0

        private fun getExternalChunkVersion(version: Byte) = (version.toInt() and 127).toByte()

        private fun sizeToSectors(size: Int) = (size - 1) / 4096 + 1

        private fun getOffsetIndex(pos: ChunkPos) = pos.regionLocalX + pos.regionLocalZ * 32
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
