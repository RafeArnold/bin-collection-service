package uk.co.rafearnold.bincollection.discordbot

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import uk.co.rafearnold.bincollection.Register
import uk.co.rafearnold.bincollection.discordbot.command.DiscordCommandHandler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class DiscordBotRegister @Inject constructor(
    private val commandHandler: DiscordCommandHandler,
    private val appProps: Map<String, String>
) : Register {

    private val connectionExecutor: Executor = Executors.newSingleThreadExecutor()

    override fun register(): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            val client: DiscordClient = DiscordClient.create(appProps.getValue("discord-bot.auth-token"))
            val connection: Mono<Void> =
                client.withGateway { gatewayClient: GatewayDiscordClient ->
                    gatewayClient.on(MessageCreateEvent::class.java) { event: MessageCreateEvent ->
                        val message: Message = event.message
                        return@on message.authorAsMember
                            .map { member: Member ->
                                commandHandler.handleCommand(
                                    userId = member.id.asString(),
                                    command = message.content,
                                    messageChannel = message.restChannel,
                                    userDisplayName = member.displayName
                                )
                            }
                    }
                }
            connectionExecutor.execute {
                try {
                    connection.block()
                } catch (e: Throwable) {
                    log.error("HTTP server failed", e)
                }
            }
        }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DiscordBotRegister::class.java)
    }
}
