package site.neworld.objective.utils

class ChunkPos(val x: Int, val z: Int) {
    fun toLong() = asLong(x, z)
    val regionX get() = x shr 5
    val regionZ get() = z shr 5
    val regionLocalX get() = x and 31
    val regionLocalZ get() = z and 31
    override fun toString() = "[$x, $z]"
    override fun equals(other: Any?) = (this === other || (other is ChunkPos && (x == other.x && z == other.z)))
    override fun hashCode() = (1664525 * x + 1013904223) xor (1664525 * (z xor -559038737) + 1013904223)

    companion object {
        val INVALID_CHUNK_POS = asLong(1875016, 1875016)
        fun asLong(x: Int, z: Int) = x.toLong() and 4294967295L or (z.toLong() and 4294967295L) shl 32
    }
}
