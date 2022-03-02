package uk.co.rafearnold.bincollection.discordbot

import discord4j.core.GatewayDiscordClient
import uk.co.rafearnold.bincollection.discordbot.model.UserInfo
import java.util.concurrent.CompletableFuture

interface DiscordBotSubscriptionManager {

    fun subscribeUser(userId: String, userInfo: UserInfo, discordClient: GatewayDiscordClient): CompletableFuture<Void>

    fun unsubscribeUser(userId: String): CompletableFuture<Void>
}
