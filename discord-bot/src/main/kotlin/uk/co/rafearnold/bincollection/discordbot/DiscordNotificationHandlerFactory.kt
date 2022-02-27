package uk.co.rafearnold.bincollection.discordbot

import discord4j.rest.entity.RestChannel

class DiscordNotificationHandlerFactory {

    fun create(messageChannel: RestChannel, userDisplayName: String): DiscordNotificationHandler =
        DiscordNotificationHandler(messageChannel = messageChannel, userDisplayName = userDisplayName)
}
