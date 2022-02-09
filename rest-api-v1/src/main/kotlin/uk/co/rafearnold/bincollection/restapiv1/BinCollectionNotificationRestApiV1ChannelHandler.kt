package uk.co.rafearnold.bincollection.restapiv1

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
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import uk.co.rafearnold.bincollection.http.AbstractRoutableHttpInboundHandler
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class BinCollectionNotificationRestApiV1ChannelHandler @Inject constructor(
    private val apiService: RestApiV1Service
) : AbstractRoutableHttpInboundHandler() {

    override val methods: Set<HttpMethod> = setOf(HttpMethod.POST)
    override val path: String = "/v1/notifications"

    private var notificationSubscriptionId: String? = null

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        val requestJson: JSONObject = JSONParser().parse(msg.content().toString(Charsets.UTF_8)) as JSONObject
        val houseNumber: String = requestJson["houseNumber"] as String
        val postcode: String = requestJson["postcode"] as String
        val notificationTimes: Set<NotificationTimeSettingRestApiV1Model> =
            (requestJson["notificationTimes"] as JSONArray)
                .map {
                    val notificationTime: JSONObject = it as JSONObject
                    NotificationTimeSettingRestApiV1Model(
                        (notificationTime["daysBeforeCollection"] as Number).toInt(),
                        (notificationTime["hourOfDay"] as Number).toInt(),
                        (notificationTime["minuteOfHour"] as Number).toInt()
                    )
                }.toSet()
        val response = DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK)
        response.headers()[HttpHeaderNames.CONTENT_TYPE] = HttpHeaderValues.TEXT_EVENT_STREAM
        response.headers()[HttpHeaderNames.CACHE_CONTROL] = HttpHeaderValues.NO_CACHE
        response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
        response.headers()[HttpHeaderNames.TRANSFER_ENCODING] = HttpHeaderValues.CHUNKED
        ctx.writeAndFlush(response)
        apiService.getBinCollectionNotifications(
            houseNumber = houseNumber,
            postcode = postcode,
            notificationTimes = notificationTimes
        ) { event: NextBinCollectionRestApiV1Model ->
            CompletableFuture.runAsync {
                val notificationJson = JSONObject()
                val binTypesJson = JSONArray()
                for (binType: BinTypeRestApiV1Model in event.binTypes) {
                    val jsonString =
                        when (binType) {
                            BinTypeRestApiV1Model.GENERAL -> "GENERAL"
                            BinTypeRestApiV1Model.RECYCLING -> "RECYCLING"
                            BinTypeRestApiV1Model.ORGANIC -> "ORGANIC"
                        }
                    binTypesJson.add(jsonString)
                }
                notificationJson["binTypes"] = binTypesJson
                notificationJson["dateOfCollection"] = event.dateOfCollection.toString()
                val content: ByteBuf = Unpooled.copiedBuffer(notificationJson.toJSONString() + "\n", Charsets.UTF_8)
                ctx.writeAndFlush(DefaultHttpContent(content))
            }
        }.thenAccept { notificationSubscriptionId = it }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        notificationSubscriptionId?.let { apiService.endGetBinCollectionNotifications(subscriptionId = it) }
    }
}
