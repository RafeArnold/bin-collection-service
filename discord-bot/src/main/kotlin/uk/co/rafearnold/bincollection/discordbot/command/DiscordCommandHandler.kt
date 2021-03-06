package uk.co.rafearnold.bincollection.discordbot.command

import discord4j.core.GatewayDiscordClient
import discord4j.rest.entity.RestChannel
import java.util.concurrent.CompletableFuture

interface DiscordCommandHandler {

    fun handleCommand(
        userId: String,
        command: String,
        messageChannel: RestChannel,
        userDisplayName: String,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void>
}
