package uk.co.rafearnold.bincollection.messengerbot.handler

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.bincollection.http.AbstractRoutableHttpInboundHandler
import javax.inject.Inject

class MessengerVerificationHandler @Inject constructor(
    private val appProps: Map<String, String>
) : AbstractRoutableHttpInboundHandler() {

    override val methods: Set<HttpMethod> = setOf(HttpMethod.GET)
    override val path: String = "/messenger"

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        val httpResponse: FullHttpResponse =
            runCatching {
                log.info("Handling Messenger verification request")

                val expectedToken: String = appProps.getValue("messenger-bot.verification-token")

                val queryParams: Map<String, String> =
                    msg.uri().substringAfter('?').let { queryString: String ->
                        if ('=' !in queryString) emptyMap()
                        else queryString.split("&")
                            .associate { it.substringBefore('=') to it.substringAfter('=') }
                    }
                val mode = queryParams["hub.mode"].orEmpty()
                val token = queryParams["hub.verify_token"].orEmpty()

                if ("subscribe" == mode && expectedToken == token) {
                    log.info("Messenger verification request verified")
                    val content = Unpooled.copiedBuffer(queryParams["hub.challenge"], Charsets.UTF_8)
                    DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK, content)
                        .also {
                            it.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())
                        }
                } else {
                    log.info("Messenger verification request not verified")
                    DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.FORBIDDEN)
                        .also {
                            it.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0)
                        }
                }
            }.getOrElse { throwable: Throwable ->
                log.info("Error encountered while handling Messenger verification request", throwable)
                DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .also {
                        it.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0)
                    }
            }
        ctx.writeAndFlush(httpResponse)
            .addListener(ChannelFutureListener.CLOSE)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MessengerVerificationHandler::class.java)
    }
}
