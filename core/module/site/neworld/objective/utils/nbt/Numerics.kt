package site.neworld.objective.utils.nbt

import java.io.DataInput
import java.io.DataOutput
import kotlin.math.floor

abstract class NumericTag protected constructor() : Tag {
    abstract val asLong: Long
    abstract val asInt: Int
    abstract val asShort: Short
    abstract val asByte: Byte
    abstract val asDouble: Double
    abstract val asFloat: Float
    abstract val asNumber: Number?
}

class ByteTag private constructor(override val asByte: Byte) : NumericTag() {
    override fun write(output: DataOutput) = output.writeByte(asByte.toInt())
    override val id get() = 1.toByte()
    override val type get() = TYPE
    override fun toString() = "${asByte}b"
    override fun copy() = this
    override fun equals(other: Any?) = if (this === other) true else other is ByteTag && asByte == other.asByte
    override fun hashCode() = asByte.toInt()
    override val asLong: Long get() = asByte.toLong()
    override val asInt: Int get() = asByte.toInt()
    override val asShort: Short get() = asByte.toShort()
    override val asDouble: Double get() = asByte.toDouble()
    override val asFloat: Float get() = asByte.toFloat()
    override val asNumber: Number get() = asByte

    companion object {
        val TYPE = object : AValueTagType<ByteTag>("BYTE", "TAG_Byte") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): ByteTag {
                fence.accountBits(72L)
                return valueOf(input.readByte())
            }
        }

        private val ZERO = valueOf(0.toByte())
        private val ONE = valueOf(1.toByte())
        private val cache = Array(256) { ByteTag((it - 128).toByte()) }

        fun valueOf(byte: Byte) = cache[128 + byte]

        fun valueOf(bool: Boolean) = if (bool) ONE else ZERO
    }
}

class ShortTag private constructor(private val data: Short) : NumericTag() {
    override fun write(output: DataOutput) = output.writeShort(data.toInt())
    override val id get() = 2.toByte()
    override val type get() = TYPE
    override fun toString() = "${data}s"
    override fun copy() = this
    override fun equals(other: Any?) = if (this === other) true else other is ShortTag && data == other.data
    override fun hashCode() = data.toInt()
    override val asLong get() = data.toLong()
    override val asInt get() = data.toInt()
    override val asShort get() = data
    override val asByte get() = data.toByte()
    override val asDouble get() = data.toDouble()
    override val asFloat get() = data.toFloat()
    override val asNumber get() = data

    companion object {
        val TYPE = object : AValueTagType<ShortTag>("SHORT", "TAG_Short") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): ShortTag {
                fence.accountBits(80L)
                return valueOf(input.readShort())
            }
        }

        private val cache = Array(1153) { ShortTag((-128 + it).toShort()) }

        fun valueOf(short: Short) = if (short >= -128 && short <= 1024) cache[short + 128] else ShortTag(short)
    }
}

class IntTag private constructor(private val data: Int) : NumericTag() {
    override fun write(output: DataOutput) = output.writeInt(data)
    override val id get() = 3.toByte()
    override val type get() = TYPE
    override fun toString() = "$data"
    override fun copy() = this
    override fun equals(other: Any?) = if (this === other) true else other is IntTag && data == other.data
    override fun hashCode() = data
    override val asLong get() = data.toLong()
    override val asInt get() = data
    override val asShort get() = data.toShort()
    override val asByte get() = data.toByte()
    override val asDouble get() = data.toDouble()
    override val asFloat get() = data.toFloat()
    override val asNumber get() = data

    companion object {
        val TYPE = object : AValueTagType<IntTag>("INT", "TAG_Int") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): IntTag {
                fence.accountBits(96L)
                return valueOf(input.readInt())
            }
        }

        private val cache = Array(1153) { IntTag(-128 + it) }

        fun valueOf(int: Int) = if (int >= -128 && int <= 1024) cache[int + 128] else IntTag(int)
    }
}

class LongTag private constructor(private val data: Long) : NumericTag() {
    override fun write(output: DataOutput) = output.writeLong(data)
    override val id get() = 4.toByte()
    override val type get() = TYPE
    override fun toString() = "${data}L"
    override fun copy() = this
    override fun equals(other: Any?) = if (this === other) true else other is LongTag && data == other.data
    override fun hashCode() = (data xor data ushr 32).toInt()
    override val asLong get() = data
    override val asInt get() = data.toInt()
    override val asShort get() = data.toShort()
    override val asByte get() = data.toByte()
    override val asDouble get() = data.toDouble()
    override val asFloat get() = data.toFloat()
    override val asNumber get() = data

    companion object {
        val TYPE = object : AValueTagType<LongTag>("LONG", "TAG_Long") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): LongTag {
                fence.accountBits(128L)
                return valueOf(input.readLong())
            }
        }

        private val cache = Array(1153) { LongTag((-128 + it).toLong()) }

        fun valueOf(long: Long) = if (long >= -128L && long <= 1024L) cache[long.toInt() + 128] else LongTag(long)
    }
}

class FloatTag private constructor(private val data: Float) : NumericTag() {
    override fun write(output: DataOutput) = output.writeFloat(data)
    override val id get() = 5.toByte()
    override val type get() = TYPE
    override fun toString() = "${data}f"
    override fun copy() = this
    override fun equals(other: Any?) = if (this === other) true else other is FloatTag && data == other.data
    override fun hashCode() = java.lang.Float.floatToIntBits(data)
    override val asLong get() = data.toLong()
    override val asInt get() = floor(data).toInt()
    override val asShort get() = floor(data).toShort()
    override val asByte get() = floor(data).toByte()
    override val asDouble get() = data.toDouble()
    override val asFloat get() = data
    override val asNumber get() = data

    companion object {
        val ZERO = FloatTag(0.0f)

        val TYPE = object : AValueTagType<FloatTag>("FLOAT", "TAG_Float") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): FloatTag {
                fence.accountBits(96L)
                return valueOf(input.readFloat())
            }
        }

        fun valueOf(value: Float): FloatTag = if (value == 0.0f) ZERO else FloatTag(value)
    }
}

class DoubleTag private constructor(private val data: Double) : NumericTag() {
    override fun write(output: DataOutput) = output.writeDouble(data)
    override val id get() = 6.toByte()
    override val type get() = TYPE
    override fun toString(): String = "${data}d"
    override fun copy(): DoubleTag = this
    override fun equals(other: Any?) = if (this === other) true else other is DoubleTag && data == other.data
    override fun hashCode(): Int {
        val int = java.lang.Double.doubleToLongBits(data)
        return (int xor int ushr 32).toInt()
    }

    override val asLong get() = floor(data).toLong()
    override val asInt get() = floor(data).toInt()
    override val asShort get() = floor(data).toShort()
    override val asByte get() = floor(data).toByte()
    override val asDouble get() = data
    override val asFloat get() = data.toFloat()
    override val asNumber get() = data

    companion object {
        val ZERO = DoubleTag(0.0)

        val TYPE = object : AValueTagType<DoubleTag>("DOUBLE", "TAG_Double") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): DoubleTag {
                fence.accountBits(128L)
                return valueOf(input.readDouble())
            }
        }

        fun valueOf(value: Double) = if (value == 0.0) ZERO else DoubleTag(value)
    }

}
