package uk.co.rafearnold.bincollection.http

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import javax.inject.Inject

class HttpServerInitializer @Inject constructor(
    private val handlerProvider: HttpServerHandlerProvider
) : ChannelInitializer<Channel>() {

    override fun initChannel(ch: Channel) {
        val pipeline: ChannelPipeline =
            ch.pipeline()
                .addLast(HttpServerCodec())
                .addLast(HttpObjectAggregator(Int.MAX_VALUE))
        for (channelHandler: ChannelHandler in handlerProvider.getHandlers()) {
            pipeline.addLast(channelHandler)
        }
    }
}
