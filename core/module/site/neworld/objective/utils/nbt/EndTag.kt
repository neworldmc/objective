package site.neworld.objective.utils.nbt

import java.io.DataInput
import java.io.DataOutput

class EndTag private constructor() : Tag {
    override fun write(output: DataOutput) {}
    override val id get() = 0.toByte()
    override val type get() = TYPE
    override fun toString() = "END"
    override fun copy() = this

    companion object {
        val TYPE = object : AValueTagType<EndTag>("END", "TAG_End") {
            override fun load(input: DataInput, depth: Int, fence: SizeFence): EndTag {
                fence.accountBits(64L)
                return INSTANCE
            }
        }

        val INSTANCE = EndTag()
    }
}