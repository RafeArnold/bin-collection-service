package uk.co.rafearnold.bincollection.messengerbot

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.multibindings.Multibinder
import com.restfb.DefaultJsonMapper
import com.restfb.JsonMapper
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import uk.co.rafearnold.bincollection.messengerbot.command.MessengerCommandHandler
import uk.co.rafearnold.bincollection.messengerbot.command.MessengerCommandHandlerImpl
import uk.co.rafearnold.bincollection.messengerbot.handler.MessengerVerificationHandlerFactory
import uk.co.rafearnold.bincollection.messengerbot.handler.MessengerWebhookHandlerFactory

class MessengerBotModule : AbstractModule() {

    override fun configure() {
        bind(MessengerBotService::class.java).to(MessengerBotServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(MessengerMessageInterface::class.java).to(MessengerMessageInterfaceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(MessengerCommandHandler::class.java).to(MessengerCommandHandlerImpl::class.java).`in`(Scopes.SINGLETON)
        bind(JsonMapper::class.java).to(DefaultJsonMapper::class.java)
        bindChannelHandlerFactories()
    }

    private fun bindChannelHandlerFactories() {
        val multibinder: Multibinder<OrderedChannelHandlerFactory> =
            Multibinder.newSetBinder(binder(), OrderedChannelHandlerFactory::class.java)
        multibinder.addBinding().to(MessengerVerificationHandlerFactory::class.java).`in`(Scopes.SINGLETON)
        multibinder.addBinding().to(MessengerWebhookHandlerFactory::class.java).`in`(Scopes.SINGLETON)
    }
}
