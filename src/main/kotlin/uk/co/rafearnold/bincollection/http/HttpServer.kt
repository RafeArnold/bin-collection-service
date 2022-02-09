package uk.co.rafearnold.bincollection.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import uk.co.rafearnold.bincollection.Register
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class HttpServer @Inject constructor(
    private val appProps: Map<String, String>,
    private val serverInitializer: HttpServerInitializer
) : Register {

    override fun register(): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            val bossGroup: EventLoopGroup = NioEventLoopGroup()
            val workerGroup: EventLoopGroup = NioEventLoopGroup()
            try {
                ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(serverInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(appProps.getValue("server.port").toInt()).sync()
                    .channel().closeFuture().sync()
            } finally {
                workerGroup.shutdownGracefully()
                bossGroup.shutdownGracefully()
            }
        }
}
