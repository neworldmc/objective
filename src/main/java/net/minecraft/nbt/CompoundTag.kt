package net.minecraft.nbt

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import net.minecraft.nbt.ByteTag.Companion.valueOf
import net.minecraft.nbt.DoubleTag.Companion.valueOf
import net.minecraft.nbt.FloatTag.Companion.valueOf
import net.minecraft.nbt.IntTag.Companion.valueOf
import net.minecraft.nbt.LongTag.Companion.valueOf
import net.minecraft.nbt.ShortTag.Companion.valueOf
import net.minecraft.nbt.StringTag.Companion.quoteAndEscape
import net.minecraft.nbt.StringTag.Companion.valueOf
import net.minecraft.nbt.TagTypes.getType
import org.apache.logging.log4j.LogManager
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class CompoundTag private constructor(private val tags: MutableMap<String, Tag>) : Tag {
    constructor() : this(HashMap<String, Tag>())

    @Throws(IOException::class)
    override fun write(output: DataOutput) {
        for (key in tags.keys) writeNamedTag(key, tags[key]!!, output)
        output.writeByte(0)
    }

    val allKeys get() = tags.keys
    override val id get() = 10.toByte()
    override val type get() = TYPE
    fun size(): Int = tags.size
    val isEmpty = tags.isEmpty()
    override fun copy() = CompoundTag(Maps.newHashMap(Maps.transformValues(tags) { obj: Tag? -> obj!!.copy() }))
    override fun equals(other: Any?) = if (this === other) true else other is CompoundTag && tags == other.tags
    override fun hashCode() = tags.hashCode()
    fun put(name: String, value: Tag): Tag = tags.put(name, value)!!
    fun putByte(name: String, value: Byte) = tags.set(name, valueOf(value))
    fun putShort(name: String, value: Short) = tags.set(name, valueOf(value))
    fun putInt(name: String, value: Int) = tags.set(name, valueOf(value))
    fun putLong(name: String, value: Long) = tags.set(name, valueOf(value))
    fun putFloat(name: String, value: Float) = tags.set(name, valueOf(value))
    fun putDouble(name: String, value: Double) = tags.set(name, valueOf(value))
    fun putString(name: String, value: String) = tags.set(name, valueOf(value))
    fun putByteArray(name: String, value: ByteArray) = tags.set(name, ByteArrayTag(value))
    fun putIntArray(name: String, value: IntArray) = tags.set(name, IntArrayTag(value))
    fun putIntArray(name: String, value: List<Int>) = tags.set(name, IntArrayTag(value))
    fun putLongArray(name: String, value: LongArray) = tags.set(name, LongArrayTag(value))
    fun putLongArray(name: String, value: List<Long>) = tags.set(name, LongArrayTag(value))
    fun putBoolean(name: String, value: Boolean) = tags.set(name, valueOf(value))
    fun getByte(name: String) = if (contains(name, 99)) (tags[name] as NumericTag).asByte else 0
    fun getShort(name: String) = if (contains(name, 99)) (tags[name] as NumericTag).asShort else 0
    fun getInt(name: String) = if (contains(name, 99)) (tags[name] as NumericTag).asInt else 0
    fun getLong(name: String) = if (contains(name, 99)) (tags[name] as NumericTag).asLong else 0L
    fun getFloat(name: String) = if (contains(name, 99)) (tags[name] as NumericTag).asFloat else 0.0f
    fun getDouble(name: String) = if (contains(name, 99)) (tags[name] as NumericTag).asDouble else 0.0
    fun getString(name: String) = if (contains(name, 8)) tags[name]!!.asString else ""
    fun getByteArray(name: String) = if (contains(name, 7)) (tags[name] as ByteArrayTag).asByteArray else ByteArray(0)
    fun getIntArray(name: String) = if (contains(name, 11)) (tags[name] as IntArrayTag).asIntArray else IntArray(0)
    fun getLongArray(name: String) = if (contains(name, 12)) (tags[name] as LongArrayTag).asLongArray else LongArray(0)
    fun getCompound(name: String) = if (this.contains(name, 10)) tags[name] as CompoundTag else CompoundTag()
    fun getBoolean(name: String) = getByte(name).toInt() != 0
    operator fun get(name: String) = tags[name]
    fun getTagType(name: String) = tags[name]?.id ?: 0
    operator fun contains(name: String) = tags.containsKey(name)
    fun getUUID(name: String) = UUID(getLong(name + "Most"), getLong(name + "Least"))
    fun hasUUID(name: String) = this.contains(name + "Most", 99) && this.contains(name + "Least", 99)

    fun putUUID(name: String, value: UUID) {
        putLong(name + "Most", value.mostSignificantBits)
        putLong(name + "Least", value.leastSignificantBits)
    }

    fun removeUUID(name: String) {
        this.remove(name + "Most")
        this.remove(name + "Least")
    }

    fun contains(name: String, desired: Int): Boolean {
        val type = getTagType(name).toInt()
        return when {
            type == desired -> true
            desired != 99 -> false
            else -> type == 1 || type == 2 || type == 3 || type == 4 || type == 5 || type == 6
        }
    }

    fun getList(name: String, eleType: Int): ListTag {
        if (getTagType(name).toInt() == 9) {
            val tag = tags[name] as ListTag
            if (tag.isEmpty() || tag.elementType == eleType) return tag
        }
        return ListTag()
    }

    fun remove(name: String) {
        tags.remove(name)
    }

    override fun toString(): String {
        val builder = StringBuilder("{")
        var keys: Collection<String> = tags.keys
        if (LOGGER.isDebugEnabled) {
            val keyList = Lists.newArrayList(tags.keys)
            keyList.sort()
            keys = keyList
        }
        for (o in keys) {
            if (builder.length != 1) builder.append(',')
            builder.append(handleEscape(o)).append(':').append(tags[o])
        }
        return builder.append('}').toString()
    }

    fun merge(other: CompoundTag): CompoundTag {
        for ((key, value) in other.tags) {
            if (value.id.toInt() == 10) {
                if (this.contains(key, 10)) getCompound(key).merge(value as CompoundTag) else put(key, value.copy())
            } else put(key, value.copy())
        }
        return this
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        val TYPE = object : ATagType<CompoundTag>("COMPOUND", "TAG_Compound") {
            @Throws(IOException::class)
            override fun load(input: DataInput, depth: Int, fence: SizeFence): CompoundTag {
                fence.accountBits(384L)
                if (depth > 512) throw RuntimeException("Tried to read NBT tag with too high complexity, depth > 512")
                val map = HashMap<String, Tag>()
                var type: Int
                while (readNamedTagType(input).also { type = it.toInt() }.toInt() != 0) {
                    val name = readNamedTagName(input)
                    fence.accountBits(224 + 16 * name.length.toLong())
                    if (map.put(name, getType(type).load(input, depth + 1, fence)) != null) fence.accountBits(288L)
                }
                return CompoundTag(map)
            }
        }
    }
}

private val SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+")

private fun writeNamedTag(name: String, tag: Tag, output: DataOutput) {
    output.writeByte(tag.id.toInt())
    if (tag.id.toInt() != 0) {
        output.writeUTF(name)
        tag.write(output)
    }
}

private fun readNamedTagType(input: DataInput) = input.readByte()

private fun readNamedTagName(input: DataInput) = input.readUTF()

private fun handleEscape(local54_0: String): String {
    return if (SIMPLE_VALUE.matcher(local54_0).matches()) local54_0 else quoteAndEscape(local54_0)
}
