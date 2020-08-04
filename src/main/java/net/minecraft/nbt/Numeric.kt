package net.minecraft.nbt

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

    override fun getId(): Byte = 1

    override fun getType() = TYPE

    override fun toString() = asByte.toString() + "b"

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
        @JvmField
        val TYPE = object : TagType<ByteTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): ByteTag {
                fence.accountBits(72L)
                return valueOf(input.readByte())
            }

            override fun getName() = "BYTE"

            override fun getPrettyName() = "TAG_Byte"

            override fun isValue() = true
        }

        private val ZERO = valueOf(0.toByte())
        private val ONE = valueOf(1.toByte())
        private val cache = Array(256) { ByteTag((it - 128).toByte()) }

        @JvmStatic
        fun valueOf(local1_0: Byte) = cache[128 + local1_0]

        @JvmStatic
        fun valueOf(local2_0: Boolean) = if (local2_0) ONE else ZERO
    }
}

class ShortTag private constructor(private val data: Short) : NumericTag() {
    override fun write(output: DataOutput) = output.writeShort(data.toInt())

    override fun getId(): Byte = 2

    override fun getType() = TYPE

    override fun toString() = data.toString() + "s"

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
        @JvmField
        val TYPE = object : TagType<ShortTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): ShortTag {
                fence.accountBits(80L)
                return valueOf(input.readShort())
            }

            override fun getName() = "SHORT"

            override fun getPrettyName() = "TAG_Short"

            override fun isValue() = true
        }

        private val cache = Array(1153) { ShortTag((-128 + it).toShort()) }

        @JvmStatic
        fun valueOf(short: Short) = if (short >= -128 && short <= 1024) cache[short + 128] else ShortTag(short)
    }
}

class IntTag private constructor(private val data: Int) : NumericTag() {
    override fun write(output: DataOutput) = output.writeInt(data)

    override fun getId(): Byte = 3

    override fun getType() = TYPE

    override fun toString() = data.toString()

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
        @JvmField
        val TYPE = object : TagType<IntTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): IntTag {
                fence.accountBits(96L)
                return valueOf(input.readInt())
            }

            override fun getName() = "INT"

            override fun getPrettyName() = "TAG_Int"

            override fun isValue() = true
        }

        private val cache = Array(1153) { IntTag(-128 + it) }

        @JvmStatic
        fun valueOf(int: Int): IntTag {
            return if (int >= -128 && int <= 1024) cache[int + 128] else IntTag(int)
        }
    }
}

class LongTag private constructor(private val data: Long) : NumericTag() {
    override fun write(output: DataOutput) = output.writeLong(data)

    override fun getId(): Byte = 4

    override fun getType() = TYPE

    override fun toString() = data.toString() + "L"

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
        @JvmField
        val TYPE = object : TagType<LongTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): LongTag {
                fence.accountBits(128L)
                return valueOf(input.readLong())
            }

            override fun getName() = "LONG"

            override fun getPrettyName() = "TAG_Long"

            override fun isValue() = true
        }

        private val cache = Array(1153) { LongTag((-128 + it).toLong()) }

        @JvmStatic
        fun valueOf(long: Long) = if (long >= -128L && long <= 1024L) cache[long.toInt() + 128] else LongTag(long)
    }
}

class FloatTag private constructor(private val data: Float) : NumericTag() {
    override fun write(output: DataOutput) = output.writeFloat(data)

    override fun getId(): Byte = 5

    override fun getType() = TYPE

    override fun toString() = data.toString() + "f"

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
        @JvmField
        val TYPE = object : TagType<FloatTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): FloatTag {
                fence.accountBits(96L)
                return valueOf(input.readFloat())
            }

            override fun getName() = "FLOAT"

            override fun getPrettyName() = "TAG_Float"

            override fun isValue() = true
        }

        @JvmStatic
        fun valueOf(local1_0: Float): FloatTag = if (local1_0 == 0.0f) ZERO else FloatTag(local1_0)
    }
}

class DoubleTag private constructor(private val data: Double) : NumericTag() {
    override fun write(output: DataOutput) = output.writeDouble(data)

    override fun getId(): Byte = 6

    override fun getType(): TagType<DoubleTag> = TYPE

    override fun toString(): String = data.toString() + "d"

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
        @JvmField
        val TYPE= object : TagType<DoubleTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): DoubleTag {
                fence.accountBits(128L)
                return valueOf(input.readDouble())
            }

            override fun getName() = "DOUBLE"

            override fun getPrettyName() = "TAG_Double"

            override fun isValue() = true
        }

        @JvmStatic
        fun valueOf(local1_0: Double) = if (local1_0 == 0.0) ZERO else DoubleTag(local1_0)
    }

}
