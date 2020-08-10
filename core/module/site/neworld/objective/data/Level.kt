package site.neworld.objective.data

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import kotlinx.coroutines.asCoroutineDispatcher
import site.neworld.objective.IBinaryAnvilEntryProvider
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.ExceptionAggregator
import java.io.File
import java.util.concurrent.Executors

class Level(private val folder: File) : AutoCloseable, IBinaryAnvilEntryProvider {
    private val regionCache = Long2ObjectLinkedOpenHashMap<Region>()
    private val oversized = LargeEntryStorage(folder.toPath())

    init {
        if (!folder.exists()) folder.mkdirs()
    }

    private suspend fun getRegionFile(pos: ChunkPos): Region {
        val regionKey = pos.toLong()
        val cacheRegion = regionCache.getAndMoveToFirst(regionKey)
        if (cacheRegion != null) return cacheRegion
        if (regionCache.size >= 256) regionCache.removeLast().close()
        val mcaFile = SectorFile.open(File(folder, "r.${pos.regionX}.${pos.regionZ}.mca").toPath())
        val loadedRegion = Region(mcaFile, oversized)
        regionCache.putAndMoveToFirst(regionKey, loadedRegion)
        return loadedRegion
    }

    override suspend fun get(pos: ChunkPos) = getRegionFile(pos).read(pos)

    override suspend fun set(pos: ChunkPos, bytes: ByteArray) = getRegionFile(pos).write(pos, bytes)

    override fun close() {
        val aggregator = ExceptionAggregator()
        for (regionFile in regionCache.values) aggregator.runCaptured { regionFile.close() }
        aggregator.complete()
    }

    companion object {
        private val asyncComputeContext = Executors.newWorkStealingPool().asCoroutineDispatcher()
    }
}
