package site.neworld.objective.data

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
import kotlinx.coroutines.*
import site.neworld.objective.IBinaryAnvilEntryProvider
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.ExceptionAggregator
import java.io.*
import java.nio.ByteBuffer
import java.nio.file.Path

private class ReorderData {
    var version: Deferred<ByteArray?>? = null
    var writeJob: Deferred<Unit>? = null
    var reference = 0
}

private val asyncContext = Concurrency.defaultCoroutineContext

private object CompressionHandler {
    private fun readStream(version: Byte, inputStream: InputStream) =
            Compression.fromId(version.toInt()).wrap(inputStream).use { it.readAllBytes() }

    private fun writeStream(version: Byte, outputStream: OutputStream, data: ByteArray) =
            Compression.fromId(version.toInt()).wrap(outputStream).use { it.write(data) }

    suspend fun inflate(method: Byte, buffer: ByteBuffer): ByteArray = withContext(asyncContext) {
        val stream = ByteArrayInputStream(buffer.array(), buffer.position(), buffer.remaining())
        readStream(method, stream)
    }

    suspend fun deflate(method: Byte, data: ByteArray): ByteBuffer = withContext(asyncContext) {
        object : ByteArrayOutputStream(8192) {
            fun toByteBuffer() = ByteBuffer.wrap(buf, 0, count)
        }.also { writeStream(method, it, data) }.toByteBuffer()
    }
}

private class AsynchronousRegionStorage(path: Path, oversize: OversizeStorage, private val method: Byte, stall: Job? = null) {
    private var init: Deferred<Unit>? = null
    private var storage: RegionStorage? = null
    private val reorder = Short2ObjectOpenHashMap<ReorderData>()
    private val scope = CoroutineScope(Concurrency.newSynchronizedCoroutineContext())
    private val context = scope.coroutineContext

    init {
        init = GlobalScope.async(asyncContext) {
            stall?.join()
            val mcaFile = SectorFile.open(path)
            storage = RegionStorage(mcaFile, oversize)
            init = null
        }
    }

    suspend fun get(pos: ChunkPos) = withContext(context) {
        val index = toIndex(pos)
        val trail = tryFetchReorder(index)
        if (trail != null) trail
        else {
            val next = async(context) { execGet(pos) }
            val reorder = forceReorder(index).apply { version = next }
            next.await().also { releaseReorder(index, reorder) }
        }
    }

    suspend fun set(pos: ChunkPos, bytes: ByteArray) = withContext(context) {
        val index = toIndex(pos)
        val reorder = forceReorder(index).apply {
            version = CompletableDeferred(bytes)
            val last = writeJob
            writeJob = async(context) { execPut(pos, bytes, last) }
            writeJob?.await()
        }
        releaseReorder(index, reorder)
    }

    suspend fun closeAsync() {
        context[Job]?.join()
        storage().closeAsync()
    }

    private fun toIndex(pos: ChunkPos) = (pos.regionLocalX or (pos.regionLocalZ shl 5)).toShort()

    private suspend fun execGet(pos: ChunkPos): ByteArray? {
        val pack = storage().read(pos) ?: return null
        return CompressionHandler.inflate(pack.first, pack.second)
    }

    private suspend fun execPut(pos: ChunkPos, bytes: ByteArray, preceding: Deferred<Unit>?) {
        val deflated = CompressionHandler.deflate(method, bytes)
        preceding?.await()
        storage().write(pos, deflated, method.toInt())
    }

    private suspend fun storage(): RegionStorage {
        val init0 = init
        if (init0 != null && !init0.isCompleted) init0.await()
        return storage!!
    }

    private suspend fun tryFetchReorder(pos: Short): ByteArray? {
        val reorder = reorder[pos]
        if (reorder != null) {
            val version = reorder.version
            if (version != null) return version.await()
        }
        return null
    }

    private fun forceReorder(pos: Short) = reorder[pos] ?: reorder.put(pos, ReorderData()).apply { ++reference }

    private fun releaseReorder(pos: Short, entry: ReorderData) {
        if (--entry.reference == 0) reorder.remove(pos)
    }
}

class LevelStorage(private val folder: File, private val version: Compression = Compression.DEFLATE) : AutoCloseable, IBinaryAnvilEntryProvider {
    private val syncContext = Concurrency.newSynchronizedCoroutineContext()
    private val regionCache = Long2ObjectLinkedOpenHashMap<AsynchronousRegionStorage>()
    private val closeSet = Long2ObjectOpenHashMap<Job>()
    private val oversized = OversizeStorage(folder.toPath())

    init {
        if (!folder.exists()) folder.mkdirs()
    }

    private fun locate(pos: ChunkPos): AsynchronousRegionStorage {
        val regionKey = getKey(pos)
        return synchronized(regionCache) {
            val cacheRegion = regionCache.getAndMoveToFirst(regionKey)
            if (cacheRegion == null) {
                if (regionCache.size >= 256) runCloseAsync(regionCache.lastLongKey(), regionCache.removeLast())
                regionCache.putAndMoveToFirst(regionKey, open(pos, regionKey))
            } else cacheRegion
        }
    }

    private fun runCloseAsync(key: Long, target: AsynchronousRegionStorage) {
        closeSet[key] = GlobalScope.launch(asyncContext) { target.closeAsync() }
    }

    private fun open(pos: ChunkPos, key: Long) =
            AsynchronousRegionStorage(path(pos), oversized, version.id.toByte(), closeSet[key])

    override suspend fun get(pos: ChunkPos) = locate(pos).get(pos)

    override suspend fun set(pos: ChunkPos, bytes: ByteArray) = locate(pos).set(pos, bytes)

    override fun close() = runBlocking {
        ExceptionAggregator().run { for ((_, v) in regionCache) runCaptured { v.closeAsync() } }
    }

    private fun path(pos: ChunkPos) = File(folder, "r.${pos.regionX}.${pos.regionZ}.mca").toPath()

    private fun getKey(pos: ChunkPos) = pos.regionX.toLong() or (pos.regionZ.toLong() shl 5)
}
