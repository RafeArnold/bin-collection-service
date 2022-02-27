package uk.co.rafearnold.bincollection.discordbot

import discord4j.rest.entity.RestChannel
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture

interface DiscordBotService {

    fun setUserAddress(
        userId: String,
        postcode: String,
        houseNumber: String,
        messageChannel: RestChannel,
        userDisplayName: String
    ): CompletableFuture<Void>

    fun addUserNotificationTime(
        userId: String,
        notificationTimeSetting: NotificationTimeSetting,
        messageChannel: RestChannel,
        userDisplayName: String
    ): CompletableFuture<Void>

    fun clearUser(userId: String): CompletableFuture<Void>
}
