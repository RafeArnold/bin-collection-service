package uk.co.rafearnold.bincollection.http

import io.netty.channel.ChannelHandler
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import javax.inject.Inject

class HttpServerHandlerProvider @Inject constructor(
    handlerFactories: Set<@JvmSuppressWildcards OrderedChannelHandlerFactory>
) {

    private val handlerFactories: List<OrderedChannelHandlerFactory> = handlerFactories.sortedBy { it.order }

    fun getHandlers(): Iterable<ChannelHandler> =
        object : Iterable<ChannelHandler> {
            private val factoryIterator: Iterator<OrderedChannelHandlerFactory> = handlerFactories.iterator()
            override fun iterator(): Iterator<ChannelHandler> =
                object : Iterator<ChannelHandler> {
                    override fun hasNext(): Boolean = factoryIterator.hasNext()
                    override fun next(): ChannelHandler = factoryIterator.next().create()
                }
        }
}
