package site.neworld.objective.io

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import net.minecraft.core.ChunkPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.*

class Region(private val folder: File) : AutoCloseable {
    private val regionCache = Long2ObjectLinkedOpenHashMap<RegionFile>()

    private fun getRegionFile(pos: ChunkPos): RegionFile {
        val regionKey = ChunkPos.asLong(pos.regionX, pos.regionZ)
        val cacheRegion = regionCache.getAndMoveToFirst(regionKey)
        return if (cacheRegion != null) {
            cacheRegion
        } else {
            if (regionCache.size >= 256) {
                regionCache.removeLast().close()
            }
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val mcaFile = File(folder, "r." + pos.regionX + "." + pos.regionZ + ".mca")
            val loadedRegion = RegionFile(mcaFile, folder)
            regionCache.putAndMoveToFirst(regionKey, loadedRegion)
            loadedRegion
        }
    }

    fun read(pos: ChunkPos): CompoundTag? {
        val regionFile = getRegionFile(pos)
        return regionFile.read(pos)?.let { DataInputStream(ByteArrayInputStream(it)).use { NbtIo.read(it) } }
    }

    fun write(pos: ChunkPos, nbt: CompoundTag) {
        val regionFile = getRegionFile(pos)
        val outputStream = ByteArrayOutputStream()
        DataOutputStream(outputStream).use { NbtIo.write(nbt, it) }
        regionFile.write(pos, outputStream.toByteArray())
    }

    override fun close() {
        for (regionFile in regionCache.values) {
            regionFile.close()
        }
    }

}
