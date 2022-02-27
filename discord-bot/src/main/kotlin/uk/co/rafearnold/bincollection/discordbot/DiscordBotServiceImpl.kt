package uk.co.rafearnold.bincollection.discordbot

import discord4j.rest.entity.RestChannel
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject

class DiscordBotServiceImpl @Inject constructor(
    private val binCollectionService: BinCollectionService,
    private val handlerFactory: DiscordNotificationHandlerFactory
) : DiscordBotService {

    private val userInfo: ConcurrentMap<String, UserInfo> = ConcurrentHashMap()

    override fun setUserAddress(
        userId: String,
        postcode: String,
        houseNumber: String,
        messageChannel: RestChannel,
        userDisplayName: String
    ): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            val userInfo: UserInfo =
                userInfo.computeIfAbsent(userId) {
                    UserInfo(
                        subscriptionId = null,
                        houseNumber = houseNumber,
                        postcode = postcode,
                        notificationTimes = mutableSetOf()
                    )
                }
            userInfo.houseNumber = houseNumber
            userInfo.postcode = postcode
            updateUserSubscription(
                messageChannel = messageChannel,
                userInfo = userInfo,
                userDisplayName = userDisplayName
            )
        }

    override fun addUserNotificationTime(
        userId: String,
        notificationTimeSetting: NotificationTimeSetting,
        messageChannel: RestChannel,
        userDisplayName: String
    ): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            val userInfo: UserInfo = userInfo[userId] ?: throw NoUserInfoFoundException(userId = userId)
            userInfo.notificationTimes.add(notificationTimeSetting)
            updateUserSubscription(
                messageChannel = messageChannel,
                userInfo = userInfo,
                userDisplayName = userDisplayName
            )
        }

    override fun clearUser(userId: String): CompletableFuture<Void> =
        CompletableFuture.runAsync { userInfo.remove(userId)?.let { unsubscribeUser(it) } }

    @Synchronized
    private fun updateUserSubscription(messageChannel: RestChannel, userInfo: UserInfo, userDisplayName: String) {
        unsubscribeUser(userInfo = userInfo)
        if (userInfo.notificationTimes.isNotEmpty()) {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                houseNumber = userInfo.houseNumber,
                postcode = userInfo.postcode,
                notificationTimes = userInfo.notificationTimes,
                notificationHandler = handlerFactory.create(
                    messageChannel = messageChannel,
                    userDisplayName = userDisplayName
                )
            ).thenAccept { subscriptionId: String -> userInfo.subscriptionId = subscriptionId }
        }
    }

    private fun unsubscribeUser(userInfo: UserInfo) {
        userInfo.subscriptionId
            ?.let { binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = it) }
    }
}
