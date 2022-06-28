package uk.co.rafearnold.bincollection.discordbot

import discord4j.core.GatewayDiscordClient
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture

interface DiscordBotService {

    fun setUserAddress(
        userId: String,
        postcode: String,
        houseNumber: String,
        userDisplayName: String,
        discordChannelId: String,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void>

    fun addUserNotificationTime(
        userId: String,
        notificationTimeSetting: NotificationTimeSetting,
        userDisplayName: String,
        discordChannelId: String,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void>

    fun clearUser(userId: String): CompletableFuture<Void>

    fun loadUsers(discordClient: GatewayDiscordClient): CompletableFuture<Void>

    fun getNextBinCollection(userId: String): CompletableFuture<NextBinCollection>
}
