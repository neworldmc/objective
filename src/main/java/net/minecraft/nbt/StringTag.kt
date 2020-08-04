package net.minecraft.nbt

import java.io.DataInput
import java.io.DataOutput

class StringTag private constructor(private val data: String) : Tag {
    override fun write(output: DataOutput) = output.writeUTF(data)

    override fun getId(): Byte = 8

    override fun getType() = TYPE

    override fun toString() = quoteAndEscape(data)

    override fun copy() = this

    override fun equals(other: Any?) = if (this === other) true else other is StringTag && data == other.data

    override fun hashCode() = data.hashCode()

    override fun getAsString() = data

    companion object {
        @JvmField
        val TYPE = object : TagType<StringTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): StringTag {
                fence.accountBits(288L)
                val local1_4 = input.readUTF()
                fence.accountBits(16 * local1_4.length.toLong())
                return valueOf(local1_4)
            }

            override fun getName() = "STRING"

            override fun getPrettyName() = "TAG_String"

            override fun isValue() = true
        }

        private val EMPTY = StringTag("")

        @JvmStatic
        fun valueOf(local1_0: String) = if (local1_0.isEmpty()) EMPTY else StringTag(local1_0)

        @JvmStatic
        fun quoteAndEscape(string: String): String {
            val builder = StringBuilder(" ")
            var quote = 0.toChar()
            for (element in string) {
                when (element) {
                    '\\' -> builder.append('\\')
                    '"', '\'' -> {
                        if (quote.toInt() == 0) quote = if (element == '"') 39.toChar() else 34.toChar()
                        if (quote == element) builder.append('\\')
                    }
                }
                builder.append(element)
            }
            if (quote.toInt() == 0) quote = 34.toChar()
            builder.setCharAt(0, quote)
            builder.append(quote)
            return builder.toString()
        }
    }
}