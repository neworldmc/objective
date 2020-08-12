package site.neworld.objective.data

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import kotlinx.coroutines.*
import site.neworld.objective.IBinaryAnvilEntryProvider
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.ExceptionAggregator
import java.io.File

private class ReorderData {
    var version: Deferred<ByteArray?>? = null
    var writeJob: Deferred<Unit>? = null
    var reference = 0
}

private class RegionStorageEntry {
    var init: Deferred<Unit>? = null
    var storage: RegionStorage? = null

    suspend fun get(): RegionStorage {
        val init0 = init
        if (init0 != null && !init0.isCompleted) init0.await()
        return storage!!
    }
}

class LevelStorage(private val folder: File) : AutoCloseable, IBinaryAnvilEntryProvider {
    private val syncContext = Concurrency.newSynchronizedCoroutineContext()
    private val regionCache = Long2ObjectLinkedOpenHashMap<RegionStorageEntry>()
    private val reorderMap = Long2ObjectOpenHashMap<ReorderData>()
    private val oversized = OversizeStorage(folder.toPath())

    init {
        if (!folder.exists()) folder.mkdirs()
    }

    private suspend fun getRegionFile(pos: ChunkPos): RegionStorage {
        val regionKey = pos.toLong()
        val cacheRegion = regionCache.getAndMoveToFirst(regionKey)
        if (cacheRegion != null) return cacheRegion.get()
        if (regionCache.size >= 256) regionCache.removeLast().get().close()
        val entry = regionCache.putAndMoveToFirst(regionKey, RegionStorageEntry())
        entry.init = GlobalScope.async(asyncContext) {
            val mcaFile = SectorFile.open(File(folder, "r.${pos.regionX}.${pos.regionZ}.mca").toPath())
            entry.storage = RegionStorage(mcaFile, oversized)
        }
        return entry.get()
    }

    private suspend fun tryFetchReorder(pos: Long): ByteArray? {
        val reorder = reorderMap[pos]
        if (reorder != null) {
            val version = reorder.version
            if (version != null) return version.await()
        }
        return null
    }

    private fun forceReorder(pos: Long) = reorderMap[pos] ?: reorderMap.put(pos, ReorderData()).apply { ++reference }

    private fun releaseReorder(pos: Long, reorder: ReorderData) {
        if (--reorder.reference == 0) reorderMap.remove(pos)
    }

    override suspend fun get(pos: ChunkPos) = withContext(syncContext) {
        val packed = pos.toLong()
        val trail = tryFetchReorder(packed)
        if (trail != null) trail
        else {
            val region = getRegionFile(pos)
            val next = async(asyncContext) { region.read(pos) }
            val reorder = forceReorder(packed).apply { version = next }
            next.await().also { releaseReorder(packed, reorder) }
        }
    }

    override suspend fun set(pos: ChunkPos, bytes: ByteArray) = withContext(syncContext) {
        val packed = pos.toLong()
        val region = getRegionFile(pos)
        val reorder = forceReorder(packed).apply {
            version = CompletableDeferred(bytes)
            val last = writeJob
            writeJob = async(asyncContext) {
                last?.await()
                region.write(pos, bytes)
            }
            writeJob?.await()
        }
        releaseReorder(packed, reorder)
    }

    override fun close() = runBlocking {
        ExceptionAggregator().run { for (v in regionCache.values) runCaptured { v.get().close() } }
    }

    companion object {
        private val asyncContext = Concurrency.defaultCoroutineContext
    }
}
