package net.minecraft.nbt

import java.io.DataInput
import java.io.DataOutput

interface Tag {
    fun write(output: DataOutput)
    val id: Byte
    val type: TagType<*>
    fun copy(): Tag
    val asString get() = this.toString()
}

interface TagType<T : Tag> {
    fun load(input: DataInput, depth: Int, fence: SizeFence): T
    fun isValue() = false
    val name: String
    val prettyName: String
}

abstract class ATagType<T : Tag>(final override val name: String, final override val prettyName: String) : TagType<T>

abstract class AValueTagType<T : Tag>(final override val name: String, final override val prettyName: String) : TagType<T> {
    final override fun isValue() = true
}

private fun createInvalid(id: Int) = object : ATagType<EndTag>("INVALID[$id]", "UNKNOWN_$id") {
    override fun load(input: DataInput, depth: Int, fence: SizeFence): EndTag {
        throw IllegalArgumentException("Invalid tag id: $id")
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
