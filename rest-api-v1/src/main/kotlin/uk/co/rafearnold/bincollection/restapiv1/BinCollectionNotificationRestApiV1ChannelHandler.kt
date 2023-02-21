package uk.co.rafearnold.bincollection.restapiv1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import uk.co.rafearnold.bincollection.http.AbstractRoutableHttpInboundHandler
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class BinCollectionNotificationRestApiV1ChannelHandler @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val apiService: RestApiV1Service
) : AbstractRoutableHttpInboundHandler() {

    override val methods: Set<HttpMethod> = setOf(HttpMethod.POST)
    override val path: String = "/v1/notifications"

    private var notificationSubscriptionId: String? = null

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        val request: GetBinCollectionNotificationsRequestRestApiV1Model =
            objectMapper.readValue(msg.content().toString(Charsets.UTF_8))
        val response = DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK)
        response.headers()[HttpHeaderNames.CONTENT_TYPE] = HttpHeaderValues.TEXT_EVENT_STREAM
        response.headers()[HttpHeaderNames.CACHE_CONTROL] = HttpHeaderValues.NO_CACHE
        response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
        response.headers()[HttpHeaderNames.TRANSFER_ENCODING] = HttpHeaderValues.CHUNKED
        ctx.writeAndFlush(response)
        apiService.getBinCollectionNotifications(request = request) { event: NextBinCollectionRestApiV1Model ->
            CompletableFuture.runAsync {
                val responseBody =
                    GetBinCollectionNotificationsResponseRestApiV1Model(
                        binTypes = event.binTypes,
                        dateOfCollection = event.dateOfCollection,
                    )
                val responseJson: String = objectMapper.writeValueAsString(responseBody)
                val content: ByteBuf = Unpooled.copiedBuffer(responseJson + "\n", Charsets.UTF_8)
                ctx.writeAndFlush(DefaultHttpContent(content))
            }
        }.thenAccept { notificationSubscriptionId = it }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        notificationSubscriptionId?.let { apiService.endGetBinCollectionNotifications(subscriptionId = it) }
    }
}
