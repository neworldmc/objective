package net.minecraft.nbt

import java.io.DataInput
import java.io.DataOutput

class StringTag private constructor(private val data: String) : Tag {
    override fun write(output: DataOutput) = output.writeUTF(data)
    override val id get() = 8.toByte()
    override val type get() = TYPE
    override fun toString() = quoteAndEscape(data)
    override fun copy() = this
    override fun equals(other: Any?) = if (this === other) true else other is StringTag && data == other.data
    override fun hashCode() = data.hashCode()
    override val asString get() = data

    companion object {
        @JvmField
        val TYPE = object : AValueTagType<StringTag>("STRING", "TAG_String") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): StringTag {
                fence.accountBits(288L)
                val string = input.readUTF()
                fence.accountBits(16 * string.length.toLong())
                return valueOf(string)
            }
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