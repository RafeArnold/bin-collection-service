package uk.co.rafearnold.bincollection.discordbot

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import uk.co.rafearnold.bincollection.Register
import uk.co.rafearnold.bincollection.discordbot.command.DiscordCommandHandler
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class DiscordBotRegister @Inject constructor(
    private val commandHandler: DiscordCommandHandler,
    private val appProps: Map<String, String>
) : Register {

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
            connection.block()
        }
}
