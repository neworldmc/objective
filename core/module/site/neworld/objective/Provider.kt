package site.neworld.objective

import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.nbt.CompoundTag

interface IBinaryAnvilEntryProvider {
    suspend fun get(pos: ChunkPos): ByteArray?
    suspend fun set(pos: ChunkPos, bytes: ByteArray)
}

interface INbtAnvilEntryProvider {
    suspend fun get(pos: ChunkPos): CompoundTag?
    suspend fun set(pos: ChunkPos, tag: CompoundTag)
}
