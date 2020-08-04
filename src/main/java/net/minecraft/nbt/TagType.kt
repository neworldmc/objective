package net.minecraft.nbt

import java.io.DataInput
import java.io.IOException

interface TagType<T : Tag?> {
    @Throws(IOException::class)
    fun load(input: DataInput, depth: Int, fence: SizeFence): T
    fun isValue() = false
    fun getName(): String
    fun getPrettyName(): String
}

private fun createInvalid(id: Int): TagType<EndTag> {
    return object : TagType<EndTag> {
        override fun load(input: DataInput, depth: Int, fence: SizeFence): EndTag {
            throw IllegalArgumentException("Invalid tag id: $id")
        }

        override fun getName() = "INVALID[$id]"
        override fun getPrettyName() = "UNKNOWN_$id"
    }
}

object TagTypes {
    private val TYPES =
            arrayOf(EndTag.TYPE, ByteTag.TYPE, ShortTag.TYPE, IntTag.TYPE, LongTag.TYPE, FloatTag.TYPE,
                    DoubleTag.TYPE, ByteArrayTag.TYPE, StringTag.TYPE, ListTag.TYPE, CompoundTag.TYPE, IntArrayTag.TYPE,
                    LongArrayTag.TYPE)

    @JvmStatic
    fun getType(id: Int): TagType<*> = if (id >= 0 && id < TYPES.size) TYPES[id] else createInvalid(id)
}
