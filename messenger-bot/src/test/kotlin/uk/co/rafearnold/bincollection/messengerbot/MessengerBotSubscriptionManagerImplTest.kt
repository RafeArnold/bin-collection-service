package uk.co.rafearnold.bincollection.messengerbot

import io.mockk.Ordering
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import uk.co.rafearnold.bincollection.AsyncLockManagerImpl
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.messengerbot.handler.MessengerNotificationHandler
import uk.co.rafearnold.bincollection.messengerbot.handler.MessengerNotificationHandlerFactory
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MessengerBotSubscriptionManagerImplTest {

    @Test
    fun `users can be subscribed to and unsubscribed from bin collection notifications`() {
        val binCollectionService: BinCollectionService = mockk()
        val handlerFactory: MessengerNotificationHandlerFactory = mockk()
        val lockManager = AsyncLockManagerImpl()
        val manager =
            MessengerBotSubscriptionManagerImpl(
                binCollectionService = binCollectionService,
                handlerFactory = handlerFactory,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val addressInfo1: AddressInfo = mockk()
        val notificationTimes1: Set<NotificationTimeSetting> = setOf(mockk(), mockk(), mockk())
        val userInfo1 =
            UserInfo(addressInfo = addressInfo1, notificationTimes = notificationTimes1)
        val notificationHandler1: MessengerNotificationHandler = mockk()
        every { handlerFactory.create(userId = userId) } returns notificationHandler1
        val subscriptionId1 = "test_subscriptionId1"
        every {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                addressInfo = addressInfo1,
                notificationTimes = notificationTimes1,
                notificationHandler = notificationHandler1
            )
        } returns CompletableFuture.completedFuture(subscriptionId1)

        manager.subscribeUser(userId = userId, userInfo = userInfo1).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            handlerFactory.create(userId = userId)
            binCollectionService.subscribeToNextBinCollectionNotifications(
                addressInfo = addressInfo1,
                notificationTimes = notificationTimes1,
                notificationHandler = notificationHandler1
            )
        }
        confirmVerified(binCollectionService, handlerFactory)

        // Now resubscribe and verify the old subscription is removed.
        clearMocks(binCollectionService, handlerFactory)

        val addressInfo2: AddressInfo = mockk()
        val notificationTimes2: Set<NotificationTimeSetting> = setOf(mockk())
        val userInfo2 = UserInfo(addressInfo = addressInfo2, notificationTimes = notificationTimes2)
        val notificationHandler2: MessengerNotificationHandler = mockk()
        every { handlerFactory.create(userId = userId) } returns notificationHandler2
        every {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId1)
        } returns CompletableFuture.completedFuture(null)
        val subscriptionId2 = "test_subscriptionId2"
        every {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                addressInfo = addressInfo2,
                notificationTimes = notificationTimes2,
                notificationHandler = notificationHandler2
            )
        } returns CompletableFuture.completedFuture(subscriptionId2)

        manager.subscribeUser(userId = userId, userInfo = userInfo2).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId1)
            handlerFactory.create(userId = userId)
            binCollectionService.subscribeToNextBinCollectionNotifications(
                addressInfo = addressInfo2,
                notificationTimes = notificationTimes2,
                notificationHandler = notificationHandler2
            )
        }
        confirmVerified(binCollectionService, handlerFactory)

        // Now unsubscribe.
        clearMocks(binCollectionService, handlerFactory)

        every {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId2)
        } returns CompletableFuture.completedFuture(null)

        manager.unsubscribeUser(userId = userId).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId2)
        }
        confirmVerified(binCollectionService, handlerFactory)
    }
}
