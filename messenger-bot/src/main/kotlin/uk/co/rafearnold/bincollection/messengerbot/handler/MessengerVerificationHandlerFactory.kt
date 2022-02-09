package uk.co.rafearnold.bincollection.messengerbot.handler

import io.netty.channel.ChannelHandler
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import javax.inject.Inject

class MessengerVerificationHandlerFactory @Inject constructor(
    private val appProps: Map<String, String>
) : OrderedChannelHandlerFactory {
    override fun create(): ChannelHandler = MessengerVerificationHandler(appProps = appProps)
}
