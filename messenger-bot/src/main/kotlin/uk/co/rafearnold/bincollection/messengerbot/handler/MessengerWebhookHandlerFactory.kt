package uk.co.rafearnold.bincollection.messengerbot.handler

import com.restfb.JsonMapper
import io.netty.channel.ChannelHandler
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import uk.co.rafearnold.bincollection.messengerbot.command.MessengerCommandHandler
import javax.inject.Inject

class MessengerWebhookHandlerFactory @Inject constructor(
    private val commandHandler: MessengerCommandHandler,
    private val appProps: Map<String, String>,
    private val jsonMapper: JsonMapper
) : OrderedChannelHandlerFactory {
    override fun create(): ChannelHandler =
        MessengerWebhookHandler(commandHandler = commandHandler, jsonMapper = jsonMapper, appProps = appProps)
}
