package site.neworld.objective.network

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.nbt.CompoundTag
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

interface IPacket {
    fun encode(buf: ByteBuf)
    fun decode(buf: ByteBuf)
}

interface IPacketSender {
    fun send(packet: IPacket)
}

private class PingPacket : IPacket {
    var identifier: Int = 0

    override fun encode(buf: ByteBuf) {
        buf.writeInt(identifier)
    }

    override fun decode(buf: ByteBuf) {
        identifier = buf.readInt()
    }
}

private class PongPacket : IPacket {
    var identifier: Int = 0
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

private class ServiceQuery : IPacket {
    var name: String = ""

    override fun encode(buf: ByteBuf) {
        buf.writeString(name)
    }

    override fun decode(buf: ByteBuf) {
        name = buf.readString()
    }
}

private class ServiceSetId : IPacket {
    var name: String = ""
    var id: Int = 0

    override fun encode(buf: ByteBuf) {
        buf.writeString(name).writeInt(id)
    }

    override fun decode(buf: ByteBuf) {
        name = buf.readString()
        id = buf.readInt()
    }
}

private class FullChunkRequest : IPacket {
    var pos: ChunkPos = ChunkPos(0, 0)
    var tag: Int = 0

    override fun encode(buf: ByteBuf) {
        buf.writeInt(pos.x).writeInt(pos.z).writeInt(tag)
    }

    override fun decode(buf: ByteBuf) {
        val x = buf.readInt()
        val z = buf.readInt()
        pos = ChunkPos(x, z)
        tag = buf.readInt()
    }
}

private class FullChunkWriteComplete : IPacket {
    var tag: Int = 0

    override fun encode(buf: ByteBuf) {
        buf.writeInt(tag)
    }

    override fun decode(buf: ByteBuf) {
        tag = buf.readInt()
    }
}

private class ChunkData : IPacket {
    var tag: Int = 0
    var nbt: CompoundTag? = null

    override fun encode(buf: ByteBuf) {
        buf.writeInt(tag).writeNbt(nbt!!)
    }

    override fun decode(buf: ByteBuf) {
        tag = buf.readInt()
        nbt = buf.readNbt()
    }
}

interface IPacketHandler {
    fun handle(packet: IPacket, sender: IPacketSender)
}

abstract class PacketHandler<T> : IPacketHandler {
    @Suppress("UNCHECKED_CAST")
    final override fun handle(packet: IPacket, sender: IPacketSender) = handle(packet as T, sender)

    abstract fun handle(packet: T, sender: IPacketSender)
}

abstract class AsyncPacketHandler<T> : IPacketHandler {
    @Suppress("UNCHECKED_CAST")
    final override fun handle(packet: IPacket, sender: IPacketSender) {
        GlobalScope.launch { handle(packet as T, sender) }
    }

    abstract suspend fun handle(packet: T, sender: IPacketSender)
}

private object PingHandler : PacketHandler<PingPacket>() {
    override fun handle(packet: PingPacket, sender: IPacketSender) {
        val pong = PongPacket()
        pong.identifier = packet.identifier
        sender.send(pong)
    }
}

private object ServiceQueryHandler : PacketHandler<ServiceQuery>() {
    override fun handle(packet: ServiceQuery, sender: IPacketSender) {
        val setId = ServiceSetId()
        setId.id = PacketHost.idQuery(packet.name)
        setId.name = packet.name
        sender.send(setId)
    }
}

object PacketHost {
    private val idList = HashMap<String, Int>()
    private val classList = HashMap<KClass<*>, Int>()
    private val classRevList = HashMap<Int, KClass<*>>()
    private val handlerList = HashMap<Int, IPacketHandler>()

    private fun idQuery(packet: IPacket) = classList[packet::class]!!

    private fun handlePacket(packet: IPacket, id: Int, sender: IPacketSender) {
        val handler = handlerList[id]!!
        handler.handle(packet, sender)
    }

    fun idQuery(name: String) = idList[name]!!

    fun processFrame(frame: ByteBuf, sender: IPacketSender) {
        val id = frame.readVarInt()
        val packet = classRevList[id]!!.createInstance() as IPacket
        packet.decode(frame)
        handlePacket(packet, id, sender)
    }

    fun constructFrame(packet: IPacket, buf: ByteBuf) {
        buf.writeVarInt(idQuery(packet))
        packet.encode(buf)
    }

    private fun pushHandler(packetClass: KClass<*>, handler: IPacketHandler?, name: String) {
        val id: Int
        synchronized(idList) { id = idList.size; idList.put(name, id) }
        synchronized(classList) { classList.put(packetClass, id) }
        synchronized(classRevList) { classRevList.put(id, packetClass) }
        synchronized(handlerList) { if (handler != null) handlerList[id] = handler }
    }

    init {
        pushHandler(PingPacket::class, PingHandler, "System.Ping")
        pushHandler(PongPacket::class, null, "System.Pong")
        pushHandler(ServiceQuery::class, ServiceQueryHandler, "System.PacketQuery")
        pushHandler(ServiceSetId::class, null, "System.PacketSetId")
        pushHandler(FullChunkRequest::class, null, "nwmc.objective.FullChunkRead")
        pushHandler(FullChunkWriteComplete::class, null, "nwmc.objective.FullChunkWriteDone")
        pushHandler(ChunkData::class, null, "nwmc.objective.FullChunkData")
    }
}
