package site.neworld.objective.data

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import site.neworld.objective.IBinaryAnvilEntryProvider
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.ExceptionAggregator
import java.io.File

class LevelStorage(private val folder: File) : AutoCloseable, IBinaryAnvilEntryProvider {
    private val syncDispatch = Concurrency.newSynchronizedCoroutineContext()
    private val regionCache = Long2ObjectLinkedOpenHashMap<RegionStorage>()
    private val oversized = OversizeStorage(folder.toPath())

    init {
        if (!folder.exists()) folder.mkdirs()
    }

    private suspend fun getRegionFile(pos: ChunkPos): RegionStorage {
        val regionKey = pos.toLong()
        val cacheRegion = regionCache.getAndMoveToFirst(regionKey)
        if (cacheRegion != null) return cacheRegion
        if (regionCache.size >= 256) regionCache.removeLast().close()
        val mcaFile = SectorFile.open(File(folder, "r.${pos.regionX}.${pos.regionZ}.mca").toPath())
        val loadedRegion = RegionStorage(mcaFile, oversized)
        regionCache.putAndMoveToFirst(regionKey, loadedRegion)
        return loadedRegion
    }

    override suspend fun get(pos: ChunkPos) = getRegionFile(pos).read(pos)

    override suspend fun set(pos: ChunkPos, bytes: ByteArray) = getRegionFile(pos).write(pos, bytes)

    override fun close() = ExceptionAggregator().run { for (v in regionCache.values) runCaptured { v.close() } }

    companion object {
        private val asyncContext = Concurrency.defaultCoroutineContext
    }
}
