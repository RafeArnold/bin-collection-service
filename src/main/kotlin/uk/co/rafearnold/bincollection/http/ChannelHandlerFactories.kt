package uk.co.rafearnold.bincollection.http

import io.netty.channel.ChannelHandler
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory

class DefaultChannelHandlerFactory : OrderedChannelHandlerFactory {
    override val order: Int = Int.MAX_VALUE
    override fun create(): ChannelHandler = DefaultChannelHandler()
}

class LoggingChannelHandlerFactory : OrderedChannelHandlerFactory {
    override val order: Int = Int.MIN_VALUE
    override fun create(): ChannelHandler = LoggingChannelHandler()
}
