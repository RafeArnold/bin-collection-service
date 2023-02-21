package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.AsyncLockManager
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.messengerbot.handler.MessengerNotificationHandlerFactory
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject

class MessengerBotSubscriptionManagerImpl @Inject constructor(
    private val binCollectionService: BinCollectionService,
    private val handlerFactory: MessengerNotificationHandlerFactory,
    private val lockManager: AsyncLockManager
) : MessengerBotSubscriptionManager {

    private val subscriptionIds: ConcurrentMap<String, String> = ConcurrentHashMap()

    override fun subscribeUser(userId: String, userInfo: UserInfo): CompletableFuture<Void> =
        lockManager.runAsyncWithLock { subscribeUser0(userId = userId, userInfo = userInfo) }

    override fun unsubscribeUser(userId: String): CompletableFuture<Void> =
        lockManager.runAsyncWithLock { unsubscribeUser0(userId = userId) }

    private fun subscribeUser0(userId: String, userInfo: UserInfo): CompletableFuture<Void> =
        unsubscribeUser0(userId = userId)
            .thenCompose {
                if (userInfo.addressInfo != null && userInfo.notificationTimes.isNotEmpty()) {
                    binCollectionService.subscribeToNextBinCollectionNotifications(
                        addressInfo = userInfo.addressInfo,
                        notificationTimes = userInfo.notificationTimes,
                        notificationHandler = handlerFactory.create(userId = userId)
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
