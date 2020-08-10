package site.neworld.objective.network

import io.netty.buffer.ByteBuf
import site.neworld.objective.utils.nbt.CompoundTag
import site.neworld.objective.utils.nbt.NbtIo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets

fun sizeVarInt(v: Int): Int {
    return when {
        v < 0 -> 5
        v < 0x7F -> 1
        v < 0x3FFF -> 2
        v < 0x1FFFFF -> 3
        v < 0xFFFFFFF -> 4
        else -> 5
    }
}

fun ByteBuf.checkedReadVarInt(): Long {
    var shl = 0
    var result = 0
    while (shl < 33) {
        if (!isReadable) return -1
        val cur = readByte().toInt()
        result = result or (cur and 127) shl shl
        if (cur < 0) return result.toLong()
        shl += 7
    }
    throw RuntimeException("VarInt too big")
}

fun ByteBuf.readVarInt(): Int {
    var shl = 0
    var result = 0
    while (shl < 33) {
        val cur = readByte().toInt()
        result = result or (cur and 127) shl shl
        if (cur < 0) return result
        shl += 7
    }
    throw RuntimeException("VarInt too big")
}

fun ByteBuf.writeVarInt(value: Int): ByteBuf {
    var operate = value
    while (operate and -128 != 0) {
        writeByte(operate and 127 or 128)
        operate = operate ushr 7
    }
    return writeByte(operate)
}

fun ByteBuf.readByteArray() = ByteArray(readVarInt()).apply { readBytes(this) }
fun ByteBuf.readShortArray() = ShortArray(readVarInt()) { readShort() }
fun ByteBuf.readIntArray() = IntArray(readVarInt()) { readInt() }
fun ByteBuf.readLongArray() = LongArray(readVarInt()) { readLong() }
fun ByteBuf.readString() = readCharSequence(readVarInt(), StandardCharsets.UTF_8).toString()
fun ByteBuf.writeByteArray(arr: ByteArray) = writeVarInt(arr.size).writeBytes(arr)!!
fun ByteBuf.writeIntArray(arr: ShortArray) = writeVarInt(arr.size).apply { for (i in arr) writeShort(i.toInt()) }
fun ByteBuf.writeIntArray(arr: IntArray) = writeVarInt(arr.size).apply { for (i in arr) writeInt(i) }
fun ByteBuf.writeLongArray(arr: LongArray) = writeVarInt(arr.size).apply { for (i in arr) writeLong(i) }
fun ByteBuf.writeString(str: String) = writeVarInt(str.length).apply { writeCharSequence(str, StandardCharsets.UTF_8) }

// just to make lives a little easier, we do record the length of the data
fun ByteBuf.writeNbt(tag: CompoundTag) {
    val outputStream = ByteArrayOutputStream()
    DataOutputStream(outputStream).use { NbtIo.write(tag, it) }
    writeByteArray(outputStream.toByteArray())
}

fun ByteBuf.readNbt() = DataInputStream(ByteArrayInputStream(readByteArray())).use { NbtIo.read(it) }
