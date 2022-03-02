package uk.co.rafearnold.bincollection.discordbot

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.multibindings.Multibinder
import uk.co.rafearnold.bincollection.Register
import uk.co.rafearnold.bincollection.discordbot.command.DiscordCommandHandler
import uk.co.rafearnold.bincollection.discordbot.command.DiscordCommandHandlerImpl
import uk.co.rafearnold.bincollection.discordbot.repository.DiscordBotRepositoryModule

class DiscordBotModule : AbstractModule() {

    override fun configure() {
        install(DiscordBotRepositoryModule())
        bind(DiscordBotService::class.java).to(DiscordBotServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DiscordCommandHandler::class.java).to(DiscordCommandHandlerImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DiscordBotSubscriptionManager::class.java)
            .to(DiscordBotSubscriptionManagerImpl::class.java).`in`(Scopes.SINGLETON)
        bindRegisters()
    }

    private fun bindRegisters() {
        val multibinder: Multibinder<Register> = Multibinder.newSetBinder(binder(), Register::class.java)
        multibinder.addBinding().to(DiscordBotRegister::class.java).`in`(Scopes.SINGLETON)
    }
}
