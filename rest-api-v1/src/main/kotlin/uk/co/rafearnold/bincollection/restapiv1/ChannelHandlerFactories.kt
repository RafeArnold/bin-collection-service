package uk.co.rafearnold.bincollection.restapiv1

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelHandler
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import javax.inject.Inject

class BinCollectionNotificationApiV1ChannelHandlerFactory @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val apiService: RestApiV1Service
) : OrderedChannelHandlerFactory {
    override fun create(): ChannelHandler =
        BinCollectionNotificationRestApiV1ChannelHandler(objectMapper = objectMapper, apiService = apiService)
}

class GetNextBinCollectionApiV1ChannelHandlerFactory @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val apiService: RestApiV1Service
) : OrderedChannelHandlerFactory {
    override fun create(): ChannelHandler =
        GetNextBinCollectionRestApiV1ChannelHandler(objectMapper = objectMapper, apiService = apiService)
}
