package site.neworld.objective.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.core.ChunkPos
import site.neworld.objective.io.LevelContext

class ObjectiveServerChannelHandler(private val level: LevelContext) : SimpleChannelInboundHandler<InboundFrame>() {
    override fun channelRead0(ctx: ChannelHandlerContext, frame: InboundFrame) {
        when (frame.operation) {
            InboundOperation.READ -> {
                level.requestRead(frame.data)
            }
            InboundOperation.WRITE -> {
                level.requestWrite(frame.data)
            }
        }
    }

    private fun parseReadPacket(data: ByteBuf): ChunkPos {
        TODO("Not yet implemented")
    }
}