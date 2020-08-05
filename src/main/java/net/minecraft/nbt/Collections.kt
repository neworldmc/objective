package net.minecraft.nbt

import it.unimi.dsi.fastutil.longs.LongSet
import org.apache.commons.lang3.ArrayUtils
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*

abstract class CollectionTag<T : Tag?> : AbstractList<T>(), Tag {
    abstract fun setTag(index: Int, tag: Tag): Boolean
    abstract fun addTag(index: Int, tag: Tag): Boolean
}

private inline fun typeCheck(tag: Tag, fn: (NumericTag) -> Unit): Boolean {
    return if (tag is NumericTag) {
        fn(tag)
        true
    } else false
}

class ByteArrayTag(private var data: ByteArray) : CollectionTag<ByteTag>() {
    constructor(list: List<Byte>) : this(toArray(list))

    @Throws(IOException::class)
    override fun write(output: DataOutput) {
        output.writeInt(data.size)
        output.write(data)
    }

    override val id get() = 7.toByte()

    override val type get() = TYPE

    override fun toString(): String {
        val builder = StringBuilder("[B;")
        for (i in data.indices) {
            if (i != 0) builder.append(',')
            builder.append(data[i]).append('B')
        }
        return builder.append(']').toString()
    }

    override fun copy() = ByteArrayTag(data.copyInto(ByteArray(data.size)))

    override fun equals(other: Any?) =
            if (this === other) true else other is ByteArrayTag && data.contentEquals(other.data)

    override fun hashCode(): Int = data.contentHashCode()

    override val size get() = data.size

    val asByteArray get() = data

    override operator fun get(index: Int): ByteTag = ByteTag.valueOf(data[index])

    override operator fun set(index: Int, element: ByteTag): ByteTag {
        val old = data[index]
        data[index] = element.asByte
        return ByteTag.valueOf(old)
    }

    override fun add(index: Int, element: ByteTag) = run { data = ArrayUtils.insert(index, data, element.asByte) }

    override fun setTag(index: Int, tag: Tag) = typeCheck(tag) { data[index] = it.asByte }

    override fun addTag(index: Int, tag: Tag) = typeCheck(tag) { data = ArrayUtils.insert(index, data, it.asByte) }

    override fun removeAt(index: Int): ByteTag {
        val old = data[index]
        data = ArrayUtils.remove(data, index)
        return ByteTag.valueOf(old)
    }

    override fun clear() = run { data = ByteArray(0) }

    companion object {
        val TYPE = object : ATagType<ByteArrayTag>("BYTE[]", "TAG_Byte_Array") {
            @Throws(IOException::class)
            override fun load(input: DataInput, depth: Int, fence: SizeFence): ByteArrayTag {
                fence.accountBits(192L)
                fence.accountBits(8L * input.readInt().toLong())
                val array = ByteArray(input.readInt())
                input.readFully(array)
                return ByteArrayTag(array)
            }
        }

        private fun toArray(other: List<Byte>) = ByteArray(other.size) { other[it] }
    }
}

class IntArrayTag(private var data: IntArray) : CollectionTag<IntTag>() {
    constructor(list: List<Int>) : this(toArray(list))

    @Throws(IOException::class)
    override fun write(output: DataOutput) {
        output.writeInt(data.size)
        for (int in data) output.writeInt(int)
    }

    override val id get() = 11.toByte()

    override val type get() = TYPE

    override fun toString(): String {
        val builder = StringBuilder("[I;")
        for (i in data.indices) {
            if (i != 0) builder.append(',')
            builder.append(data[i])
        }
        return builder.append(']').toString()
    }

    override fun copy() = IntArrayTag(data.copyInto(IntArray(data.size)))

    override fun equals(other: Any?) =
            if (this === other) true else other is IntArrayTag && data.contentEquals(other.data)

    override fun hashCode() = data.contentHashCode()

    override val size get() = data.size

    val asIntArray: IntArray get() = data

    override operator fun get(index: Int) = IntTag.valueOf(data[index])

    override operator fun set(index: Int, element: IntTag): IntTag {
        val old = data[index]
        data[index] = element.asInt
        return IntTag.valueOf(old)
    }

    override fun add(index: Int, element: IntTag) = run { data = ArrayUtils.insert(index, data, element.asInt) }

    override fun setTag(index: Int, tag: Tag) = typeCheck(tag) { data[index] = it.asInt }

    override fun addTag(index: Int, tag: Tag) = typeCheck(tag) { data = ArrayUtils.insert(index, data, it.asInt) }

    override fun removeAt(index: Int): IntTag {
        val old = data[index]
        data = ArrayUtils.remove(data, index)
        return IntTag.valueOf(old)
    }

    override fun clear() = run { data = IntArray(0) }

    companion object {
        val TYPE = object : ATagType<IntArrayTag>("INT[]", "TAG_Int_Array") {
            @Throws(IOException::class)
            override fun load(input: DataInput, depth: Int, fence: SizeFence): IntArrayTag {
                fence.accountBits(192L)
                val size = input.readInt()
                fence.accountBits(32L * size.toLong())
                return IntArrayTag(IntArray(size) { input.readInt() })
            }
        }

        private fun toArray(other: List<Int>) = IntArray(other.size) { other[it] }
    }

}

class LongArrayTag(private var data: LongArray) : CollectionTag<LongTag>() {
    constructor(set: LongSet) : this(set.toLongArray())

    constructor(list: List<Long>) : this(toArray(list))

    @Throws(IOException::class)
    override fun write(output: DataOutput) {
        output.writeInt(data.size)
        for (local4_2 in data) output.writeLong(local4_2)
    }

    override val id get() = 12.toByte()

    override val type get() = TYPE

    override fun toString(): String {
        val builder = StringBuilder("[L;")
        for (i in data.indices) {
            if (i != 0) builder.append(',')
            builder.append(data[i]).append('L')
        }
        return builder.append(']').toString()
    }

    override fun copy() = LongArrayTag(data.copyInto(LongArray(data.size)))

    override fun equals(other: Any?) =
            if (this === other) true else other is LongArrayTag && data.contentEquals(other.data)

    override fun hashCode() = data.contentHashCode()

    val asLongArray get() = data

    override val size get() = data.size

    override operator fun get(index: Int) = LongTag.valueOf(data[index])

    override operator fun set(index: Int, element: LongTag): LongTag {
        val old = data[index]
        data[index] = element.asLong
        return LongTag.valueOf(old)
    }

    override fun add(index: Int, element: LongTag) = run { data = ArrayUtils.insert(index, data, element.asLong) }

    override fun setTag(index: Int, tag: Tag) = typeCheck(tag) { data[index] = it.asLong }

    override fun addTag(index: Int, tag: Tag) = typeCheck(tag) { data = ArrayUtils.insert(index, data, it.asLong) }

    override fun removeAt(index: Int): LongTag {
        val old = data[index]
        data = ArrayUtils.remove(data, index)
        return LongTag.valueOf(old)
    }

    override fun clear() = run { data = LongArray(0) }

    companion object {
        val TYPE = object : ATagType<LongArrayTag>("LONG[]", "TAG_Long_Array") {
            @Throws(IOException::class)
            override fun load(input: DataInput, depth: Int, fence: SizeFence): LongArrayTag {
                fence.accountBits(192L)
                val size = input.readInt()
                fence.accountBits(64L * size.toLong())
                return LongArrayTag(LongArray(size) { input.readLong() })
            }
        }

        private fun toArray(other: List<Long>) = LongArray(other.size) { other[it] }
    }
}

class ListTag private constructor(private val list: MutableList<Tag>, private var typeId: Byte) : CollectionTag<Tag>() {
    constructor() : this(mutableListOf<Tag>(), 0.toByte())

    @Throws(IOException::class)
    override fun write(output: DataOutput) {
        typeId = (if (list.isEmpty()) 0 else list[0].id)
        output.writeByte(typeId.toInt())
        output.writeInt(list.size)
        for (item in list) item.write(output)
    }

    override val id get() = 9.toByte()

    override val type get() = TYPE

    override fun toString(): String {
        val builder = StringBuilder("[")
        for (i in list.indices) {
            if (i != 0) builder.append(',')
            builder.append(list[i])
        }
        return builder.append(']').toString()
    }

    private fun updateTypeAfterRemove() = run { if (list.isEmpty()) typeId = 0 }

    override fun removeAt(index: Int): Tag {
        val old = list.removeAt(index)
        updateTypeAfterRemove()
        return old
    }

    override fun isEmpty() = list.isEmpty()

    private inline fun <T> checkedGet(index: Int, id: Int, pass: (Tag) -> T, fail: () -> T): T {
        if (index >= 0 && index < list.size) {
            val item = list[index]
            if (item.id.toInt() == id) return pass(item)
        }
        return fail()
    }

    fun getByte(index: Int) = checkedGet(index, 1, { (it as ByteTag).asByte }, { 0 })

    fun getShort(index: Int) = checkedGet(index, 2, { (it as ShortTag).asShort }, { 0 })

    fun getInt(index: Int) = checkedGet(index, 3, { (it as IntTag).asInt }, { 0 })

    fun getLong(index: Int) = checkedGet(index, 4, { (it as LongTag).asLong }, { 0 })

    fun getFloat(index: Int) = checkedGet(index, 5, { (it as FloatTag).asFloat }, { 0.0f })

    fun getDouble(index: Int) = checkedGet(index, 6, { (it as DoubleTag).asDouble }, { 0.0 })

    fun getList(index: Int) = checkedGet(index, 9, { it as ListTag }, { ListTag() })

    fun getCompound(index: Int) = checkedGet(index, 10, { it as CompoundTag }, { CompoundTag() })

    fun getIntArray(index: Int) = checkedGet(index, 11, { (it as IntArrayTag).asIntArray }, { IntArray(0) })

    fun getString(index: Int) =
            if (index >= 0 && index < list.size) {
                val item = list[index]
                if (item.id.toInt() == 8) item.asString else item.toString()
            } else ""

    override val size get() = list.size

    override operator fun get(index: Int) = list[index]

    override operator fun set(index: Int, element: Tag): Tag {
        val old = this[index]
        if (!setTag(index, element))
            throw UnsupportedOperationException("Trying to add tag of type ${element.id} to list of $typeId")
        return old
    }

    override fun add(index: Int, element: Tag) {
        if (!addTag(index, element))
            throw UnsupportedOperationException("Trying to add tag of type ${element.id} to list of $typeId")
    }

    override fun setTag(index: Int, tag: Tag): Boolean {
        return if (updateType(tag)) {
            list[index] = tag
            true
        } else false
    }

    override fun addTag(index: Int, tag: Tag): Boolean {
        return if (updateType(tag)) {
            list.add(index, tag)
            true
        } else false
    }

    private fun updateType(tag: Tag): Boolean {
        return when {
            tag.id.toInt() == 0 -> false
            typeId.toInt() == 0 -> {
                typeId = tag.id
                true
            }
            else -> typeId == tag.id
        }
    }

    override fun copy() = ListTag(if (TagTypes.getType(typeId.toInt()).isValue())
        list.toMutableList() else MutableList(list.size) { list[it].copy() }, typeId)

    override fun equals(other: Any?) = if (this === other) true else other is ListTag && list == other.list

    override fun hashCode() = list.hashCode()

    val elementType get() = typeId.toInt()

    override fun clear() {
        list.clear()
        typeId = 0
    }

    companion object {
        val TYPE = object : ATagType<ListTag>("LIST", "TAG_List") {
            @Throws(IOException::class)
            override fun load(input: DataInput, depth: Int, fence: SizeFence): ListTag {
                fence.accountBits(296L)
                if (depth > 512) throw RuntimeException("Tried to read NBT tag with too high complexity, depth > 512")
                val typeId = input.readByte().toInt()
                val size = input.readInt()
                if (typeId == 0 && size > 0) throw RuntimeException("Missing type on ListTag")
                fence.accountBits(32L * size.toLong())
                val type = TagTypes.getType(typeId)
                return ListTag(MutableList(size) { type.load(input, depth + 1, fence) }, typeId.toByte())
            }
        }
    }
}