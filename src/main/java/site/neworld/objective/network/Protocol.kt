package site.neworld.objective.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.EncoderException
import io.netty.handler.codec.MessageToByteEncoder

data class InboundFrame(val operation: InboundOperation, val data: ByteBuf)

enum class InboundOperation { READ, WRITE }

class VarInt32LengthFieldBasedFrameDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        buf.markReaderIndex()
        var index = 0
        var payloadLength = 0
        do {
            if (index >= 5) throw CorruptedFrameException("VarInt is too big")
            if (!buf.isReadable) {
                buf.resetReaderIndex()
                return
            }
            val b = buf.readUnsignedByte().toInt()
            payloadLength = payloadLength or (b and 127 shl index++ * 7)
        } while (b and 128 != 0)
        if (buf.readableBytes() < payloadLength) {
            buf.resetReaderIndex()
            return
        }
        out.add(buf.readBytes(payloadLength))
    }
}

class VarInt32LengthFieldBasedFrameEncoder : MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, frame: ByteBuf, buf: ByteBuf) {
        val varIntList = mutableListOf<Int>()
        var payloadLength = frame.readableBytes()
        while (payloadLength > 0) {
            varIntList.add(payloadLength and 127 or 128)
            payloadLength = payloadLength shr 7
        }
        if (varIntList.isEmpty()) throw EncoderException("frame is void")
        varIntList[varIntList.lastIndex] = varIntList[varIntList.lastIndex] and 127
        buf.writeBytes(varIntList.map(Int::toByte).toByteArray())
        buf.writeBytes(frame)
    }
}
