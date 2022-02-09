package uk.co.rafearnold.bincollection

import io.netty.channel.ChannelHandler

interface OrderedChannelHandlerFactory {
    val order: Int get() = 0
    fun create(): ChannelHandler
}
