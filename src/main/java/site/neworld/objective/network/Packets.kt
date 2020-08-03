package site.neworld.objective.network

import io.netty.buffer.ByteBuf
import net.minecraft.core.ChunkPos
import net.minecraft.nbt.CompoundTag
import java.time.Instant

interface Packet {
    fun encode(buf: ByteBuf)
    fun decode(buf: ByteBuf)
}

private data class PingPacket(var identifier: Int = 0) : Packet {
    override fun encode(buf: ByteBuf) {
        buf.writeInt(identifier)
    }

    override fun decode(buf: ByteBuf) {
        identifier = buf.readInt()
    }
}

private data class PongPacket(var identifier: Int = 0) : Packet {
    var timeStamp = (Instant.now().toEpochMilli() / 1000L).toInt()

    override fun encode(buf: ByteBuf) {
        buf.writeInt(identifier)
        buf.writeInt(timeStamp)
    }

    override fun decode(buf: ByteBuf) {
        identifier = buf.readInt()
        timeStamp = buf.readInt()
    }
}

private data class ServiceQuery(var name: String = ""): Packet {
    override fun encode(buf: ByteBuf) {
        buf.writeString(name)
    }

    override fun decode(buf: ByteBuf) {
        name = buf.readString()
    }
}

private data class ServiceSetId(var name: String = "", var id: Int = 0): Packet {
    override fun encode(buf: ByteBuf) {
        buf.writeString(name)
        buf.writeInt(id)
    }

    override fun decode(buf: ByteBuf) {
        name = buf.readString()
        id = buf.readInt()
    }
}

private data class FullChunkRequest(var pos: ChunkPos, var tag: Int): Packet {
    override fun encode(buf: ByteBuf) {
        buf.writeInt(pos.x)
        buf.writeInt(pos.z)
        buf.writeInt(tag)
    }

    override fun decode(buf: ByteBuf) {
        val x = buf.readInt()
        val z = buf.readInt()
        pos = ChunkPos(x, z)
        tag = buf.readInt()
    }
}

private data class FullChunkWriteComplete(var tag: Int): Packet {
    override fun encode(buf: ByteBuf) {
        buf.writeInt(tag)
    }

    override fun decode(buf: ByteBuf) {
        tag = buf.readInt()
    }
}

private data class ChunkData(var tag: Int = 0, var nbt: CompoundTag): Packet {
    override fun encode(buf: ByteBuf) {
        buf.writeInt(tag)
        buf.writeNbt(nbt)
    }

    override fun decode(buf: ByteBuf) {
        tag = buf.readInt()
        nbt = buf.readNbt()
    }
}

object PacketHost {
    private val list = HashMap<String, Int>()
}