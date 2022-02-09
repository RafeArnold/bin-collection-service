package uk.co.rafearnold.bincollection.http

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggingChannelHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        logRequest(msg)
        ctx.fireChannelRead(msg)
    }

    private fun logRequest(msg: FullHttpRequest) {
        val message: String =
            "Request received -" +
                    " method: '${msg.method().name()}'," +
                    " uri: '${msg.uri()}'," +
                    " headers: '${msg.headers().joinToString { "${it.key}: ${it.value}" }}'," +
                    " body: '${Unpooled.copiedBuffer(msg.content()).toString(Charsets.UTF_8)}'"
        log.debug(message)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LoggingChannelHandler::class.java)
    }
}
