package uk.co.rafearnold.bincollection.restapiv1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import uk.co.rafearnold.bincollection.http.AbstractRoutableHttpInboundHandler
import javax.inject.Inject

class GetNextBinCollectionRestApiV1ChannelHandler @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val apiService: RestApiV1Service
) : AbstractRoutableHttpInboundHandler() {

    override val methods: Set<HttpMethod> = setOf(HttpMethod.GET)
    override val path: String = "/v1/next"

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        val request: GetNextBinCollectionRequestRestApiV1Model =
            objectMapper.readValue(msg.content().toString(Charsets.UTF_8))
        val (nextCollection: NextBinCollectionRestApiV1Model) = apiService.getNextBinCollection(request = request).get()
        val responseBody =
            GetBinCollectionNotificationsResponseRestApiV1Model(
                binTypes = nextCollection.binTypes,
                dateOfCollection = nextCollection.dateOfCollection,
            )
        val responseJson: String = objectMapper.writeValueAsString(responseBody)
        val content: ByteBuf = Unpooled.copiedBuffer(responseJson + "\n", Charsets.UTF_8)
        val response = DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK, content)
        response.headers()[HttpHeaderNames.CONTENT_TYPE] = HttpHeaderValues.APPLICATION_JSON
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }
}
