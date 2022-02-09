package uk.co.rafearnold.bincollection.messengerbot.handler

import com.restfb.JsonMapper
import com.restfb.types.webhook.WebhookObject
import com.restfb.types.webhook.messaging.MessagingItem
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.bincollection.http.AbstractRoutableHttpInboundHandler
import uk.co.rafearnold.bincollection.messengerbot.command.MessengerCommandHandler
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class MessengerWebhookHandler @Inject constructor(
    private val commandHandler: MessengerCommandHandler,
    private val jsonMapper: JsonMapper
) : AbstractRoutableHttpInboundHandler() {

    override val methods: Set<HttpMethod> = setOf(HttpMethod.POST)
    override val path: String = "/messenger"

    private val mac: Mac =
        Mac.getInstance("HmacSHA1")
            .also { it.init(SecretKeySpec("14c7b6742dfcf8c9f28cb88dcc441abb".toByteArray(Charsets.UTF_8), "HmacSHA1")) }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        log.info("Handling Messenger webhook event")
        val httpResponse = DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK)
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0)
        ctx.writeAndFlush(httpResponse)
            .addListener(ChannelFutureListener.CLOSE)
        if (msg.hasValidSignature()) {
            log.info("Webhook event contains a valid signature")
            val requestBody: String = Unpooled.copiedBuffer(msg.content()).toString(Charsets.UTF_8)
            val webhookObject: WebhookObject = jsonMapper.toJavaObject(requestBody, WebhookObject::class.java)
            val messageEntry: MessagingItem = webhookObject.entryList.firstOrNull()?.messaging?.firstOrNull() ?: return
            val userId: String = messageEntry.sender?.id ?: return
            val messageText: String = messageEntry.message?.text ?: return
            commandHandler.handleCommand(userId = userId, command = messageText)
        } else {
            log.info("Webhook event does not contain a valid signature")
        }
    }

    private fun FullHttpRequest.hasValidSignature(): Boolean {
        val requestSignatureHeader: String = this.headers().get("X-Hub-Signature")
        if (!requestSignatureHeader.startsWith("sha1=")) return false
        val requestSignature: String = requestSignatureHeader.substring(5)
        val expectedSignature: String = bytesToHexString(mac.doFinal(Unpooled.copiedBuffer(this.content()).array()))
        return requestSignature == expectedSignature
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MessengerWebhookHandler::class.java)

        private val HEX_ARRAY: CharArray = "0123456789abcdef".toCharArray()

        /**
         * Converts the provided [bytes] into a hex encoded string.
         */
        private fun bytesToHexString(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = HEX_ARRAY[v ushr 4]
                hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
            }
            return String(hexChars)
        }
    }
}
