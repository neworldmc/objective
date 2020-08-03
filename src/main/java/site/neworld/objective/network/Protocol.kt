package site.neworld.objective.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.EncoderException
import io.netty.handler.codec.MessageToByteEncoder

data class InboundFrame(val operation: InboundOperation, val data: ByteBuf)

enum class InboundOperation { READ, WRITE }

class VarInt32LengthFieldBasedFrameDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        buf.markReaderIndex()
        val len = buf.checkedReadVarInt()
        if (len >= 0 && buf.readableBytes() >= len) out.add(buf.readBytes(len.toInt())) else buf.resetReaderIndex()
    }
}

class VarInt32LengthFieldBasedFrameEncoder : MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, frame: ByteBuf, buf: ByteBuf) {
        val payloadLength = frame.readableBytes()
        if (payloadLength == 0) throw EncoderException("void frame")
        buf.writeVarInt(payloadLength)
        buf.writeBytes(frame)
    }
}
