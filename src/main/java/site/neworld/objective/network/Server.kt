package site.neworld.objective.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import org.apache.logging.log4j.LogManager
import java.net.InetAddress

fun runServer(host: InetAddress?, port: Int) {
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    try {
        val channelFuture = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .localAddress(host, port)
                .channel(NioServerSocketChannel::class.java)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(RegionStorageServerChannelInitializer)
                .bind().sync()
        LogManager.getLogger().info("Server started on ${host?.hostAddress ?: "*"}:${port}")
        channelFuture.channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

object RegionStorageServerChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(VarInt32LengthFieldBasedFrameDecoder(), VarInt32LengthFieldBasedFrameEncoder())
        ch.pipeline().addLast(StringDecoder(Charsets.UTF_8), StringEncoder(Charsets.UTF_8))
        ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
            override fun channelActive(ctx: ChannelHandlerContext) {
                ctx.writeAndFlush("hello world".repeat(300))
            }

            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                LogManager.getLogger().info("Received: $msg")
                ctx.writeAndFlush(msg)
            }
        })
    }
}
