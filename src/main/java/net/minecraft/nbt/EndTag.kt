package net.minecraft.nbt

import java.io.DataInput
import java.io.DataOutput

class EndTag private constructor() : Tag {
    override fun write(output: DataOutput) {}

    override fun getId(): Byte = 0

    override fun getType() = TYPE

    override fun toString() = "END"

    override fun copy() = this

    companion object {
        @JvmField
        val TYPE = object : TagType<EndTag> {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): EndTag {
                fence.accountBits(64L)
                return INSTANCE
            }

            override fun getName() = "END"

            override fun getPrettyName() = "TAG_End"

            override fun isValue() = true
        }
        @JvmField
        val INSTANCE = EndTag()
    }
}