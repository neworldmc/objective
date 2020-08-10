package site.neworld.objective.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
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
                .childHandler(ObjectiveServerChannelInitializer)
                .bind().sync()
        LogManager.getLogger().info("Server started on ${host?.hostAddress ?: "*"}:${port}")
        channelFuture.channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

object ObjectiveServerChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
                .addLast(VarInt32LengthFieldBasedFrameDecoder(), VarInt32LengthFieldBasedFrameEncoder())
                .addLast(FrameHandler())
    }
}
