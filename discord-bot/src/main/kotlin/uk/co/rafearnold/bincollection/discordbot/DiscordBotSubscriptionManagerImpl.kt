package uk.co.rafearnold.bincollection.discordbot

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import uk.co.rafearnold.bincollection.AsyncLockManager
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.discordbot.model.UserInfo
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject

class DiscordBotSubscriptionManagerImpl @Inject constructor(
    private val binCollectionService: BinCollectionService,
    private val handlerFactory: DiscordNotificationHandlerFactory,
    private val lockManager: AsyncLockManager
) : DiscordBotSubscriptionManager {

    private val subscriptionIds: ConcurrentMap<String, String> = ConcurrentHashMap()

    override fun subscribeUser(
        userId: String,
        userInfo: UserInfo,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            subscribeUser0(userId = userId, userInfo = userInfo, discordClient = discordClient)
        }

    override fun unsubscribeUser(userId: String): CompletableFuture<Void> =
        lockManager.runAsyncWithLock { unsubscribeUser0(userId = userId) }

    private fun subscribeUser0(
        userId: String,
        userInfo: UserInfo,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void> =
        unsubscribeUser0(userId = userId)
            .thenCompose {
                if (userInfo.addressInfo != null && userInfo.notificationTimes.isNotEmpty()) {
                    binCollectionService.subscribeToNextBinCollectionNotifications(
                        addressInfo = userInfo.addressInfo,
                        notificationTimes = userInfo.notificationTimes,
                        notificationHandler = handlerFactory.create(
                            messageChannel = discordClient.getChannelById(Snowflake.of(userInfo.discordChannelId))
                                .block().restChannel,
                            userDisplayName = userInfo.displayName
                        )
                    ).thenAccept { subscriptionId: String -> subscriptionIds[userId] = subscriptionId }
                } else CompletableFuture.completedFuture(null)
            }

    private fun unsubscribeUser0(userId: String): CompletableFuture<Void> {
        val subscriptionId: String? = subscriptionIds.remove(userId)
        return if (subscriptionId != null) {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId)
        } else CompletableFuture.completedFuture(null)
    }
}
