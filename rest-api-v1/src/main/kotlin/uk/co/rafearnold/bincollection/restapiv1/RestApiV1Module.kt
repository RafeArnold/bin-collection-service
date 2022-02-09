package uk.co.rafearnold.bincollection.restapiv1

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.multibindings.Multibinder
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory

class RestApiV1Module : AbstractModule() {

    override fun configure() {
        bind(RestApiV1Service::class.java).to(RestApiV1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bindChannelHandlerFactories()
    }

    private fun bindChannelHandlerFactories() {
        val multibinder: Multibinder<OrderedChannelHandlerFactory> =
            Multibinder.newSetBinder(binder(), OrderedChannelHandlerFactory::class.java)
        multibinder.addBinding().to(BinCollectionNotificationApiV1ChannelHandlerFactory::class.java).`in`(Scopes.SINGLETON)
    }
}
