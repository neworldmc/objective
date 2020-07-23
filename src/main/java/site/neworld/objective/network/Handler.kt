package site.neworld.objective.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ObjectiveServerChannelHandler : SimpleChannelInboundHandler<InboundFrame>() {
    override fun channelRead0(ctx: ChannelHandlerContext, frame: InboundFrame) {
        when (frame.operation) {
            InboundOperation.READ -> {

            }
            InboundOperation.WRITE -> {

            }
        }
    }
}