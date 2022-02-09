package uk.co.rafearnold.bincollection.restapiv1

import io.netty.channel.ChannelHandler
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import javax.inject.Inject

class BinCollectionNotificationApiV1ChannelHandlerFactory @Inject constructor(
    private val apiService: RestApiV1Service
) : OrderedChannelHandlerFactory {
    override fun create(): ChannelHandler = BinCollectionNotificationRestApiV1ChannelHandler(apiService = apiService)
}
