package site.neworld.objective.utils.nbt

import site.neworld.objective.utils.nbt.TagTypes.getType
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object NbtIo {
    fun readCompressed(input: InputStream) =
            DataInputStream(BufferedInputStream(GZIPInputStream(input))).use { read(it, SizeFence.UNLIMITED) }

    fun writeCompressed(tag: CompoundTag, output: OutputStream) =
            DataOutputStream(BufferedOutputStream(GZIPOutputStream(output))).use { write(tag, it) }

    fun read(input: DataInputStream) = read(input, SizeFence.UNLIMITED)

    fun read(input: DataInput, fence: SizeFence): CompoundTag {
        val tag = readUnnamedTag(input, fence)
        return if (tag is CompoundTag) tag else throw IOException("Root tag must be a named compound tag")
    }

    fun write(tag: CompoundTag, output: DataOutput) = writeUnnamedTag(tag, output)

    private fun writeUnnamedTag(tag: Tag, output: DataOutput) {
        output.writeByte(tag.id.toInt())
        if (tag.id.toInt() != 0) {
            output.writeUTF("")
            tag.write(output)
        }
    }

    private fun readUnnamedTag(input: DataInput, fence: SizeFence): Tag {
        val type = input.readByte().toInt()
        if (type == 0) return EndTag.INSTANCE
        input.readUTF()
        return getType(type).load(input, 0, fence)
    }
}

open class SizeFence(private val quota: Long) {
    private var usage: Long = 0

    open fun accountBits(bits: Long) {
        usage += bits / 8L
        if (usage > quota) {
            throw RuntimeException("Tried to read NBT tag that was too big; tried to allocate: ${usage}bytes where max allowed: $quota")
        }
    }

    companion object {
        val UNLIMITED = object : SizeFence(0L) {
            override fun accountBits(bits: Long) {}
        }
    }
}