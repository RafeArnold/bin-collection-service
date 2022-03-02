package uk.co.rafearnold.bincollection.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.bincollection.Register
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class HttpServer @Inject constructor(
    private val appProps: Map<String, String>,
    private val serverInitializer: HttpServerInitializer
) : Register {

    private val serverExecutor: Executor = Executors.newSingleThreadExecutor()

    override fun register(): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            val bossGroup: EventLoopGroup = NioEventLoopGroup()
            val workerGroup: EventLoopGroup = NioEventLoopGroup()
            val channelFuture: ChannelFuture =
                ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(serverInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(appProps.getValue("server.port").toInt())
            serverExecutor.execute {
                try {
                    channelFuture.sync().channel().closeFuture().sync()
                } catch (e: Throwable) {
                    log.error("HTTP server failed", e)
                } finally {
                    workerGroup.shutdownGracefully()
                    bossGroup.shutdownGracefully()
                }
            }
        }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(HttpServer::class.java)
    }
}
