package uk.co.rafearnold.bincollection.discordbot

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.Channel
import discord4j.rest.entity.RestChannel
import io.mockk.Ordering
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import uk.co.rafearnold.bincollection.AsyncLockManagerImpl
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.discordbot.model.UserInfo
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class DiscordBotSubscriptionManagerImplTest {

    @Test
    fun `users can be subscribed to and unsubscribed from bin collection notifications`() {
        val binCollectionService: BinCollectionService = mockk()
        val handlerFactory: DiscordNotificationHandlerFactory = mockk()
        val lockManager = AsyncLockManagerImpl()
        val manager =
            DiscordBotSubscriptionManagerImpl(
                binCollectionService = binCollectionService,
                handlerFactory = handlerFactory,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val houseNumber1 = "test_houseNumber1"
        val postcode1 = "test_postcode1"
        val notificationTimes1: Set<NotificationTimeSetting> = setOf(mockk(), mockk(), mockk())
        val messageChannel1: RestChannel = mockk()
        val discordUserDisplayName1 = "test_discordUserDisplayName1"
        val discordChannelId1 = "9563675"
        val userInfo1 =
            UserInfo(
                houseNumber = houseNumber1,
                postcode = postcode1,
                notificationTimes = notificationTimes1,
                displayName = discordUserDisplayName1,
                discordChannelId = discordChannelId1
            )
        val notificationHandler1: DiscordNotificationHandler = mockk()
        every {
            handlerFactory.create(messageChannel = messageChannel1, userDisplayName = discordUserDisplayName1)
        } returns notificationHandler1
        val subscriptionId1 = "test_subscriptionId1"
        every {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                houseNumber = houseNumber1,
                postcode = postcode1,
                notificationTimes = notificationTimes1,
                notificationHandler = notificationHandler1
            )
        } returns CompletableFuture.completedFuture(subscriptionId1)
        val channel1: Channel = mockk()
        every { channel1.restChannel } returns messageChannel1
        val discordClient1: GatewayDiscordClient = mockk()
        every { discordClient1.getChannelById(Snowflake.of(discordChannelId1)) } returns Mono.just(channel1)

        manager.subscribeUser(userId = userId, userInfo = userInfo1, discordClient = discordClient1)
            .get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            handlerFactory.create(messageChannel = messageChannel1, userDisplayName = discordUserDisplayName1)
            binCollectionService.subscribeToNextBinCollectionNotifications(
                houseNumber = houseNumber1,
                postcode = postcode1,
                notificationTimes = notificationTimes1,
                notificationHandler = notificationHandler1
            )
        }
        confirmVerified(binCollectionService, handlerFactory)

        // Now resubscribe and verify the old subscription is removed.
        clearAllMocks()

        val houseNumber2 = "test_houseNumber2"
        val postcode2 = "test_postcode2"
        val notificationTimes2: Set<NotificationTimeSetting> = setOf(mockk())
        val messageChannel2: RestChannel = mockk()
        val discordUserDisplayName2 = "test_discordUserDisplayName2"
        val discordChannelId2 = "2534675685"
        val userInfo2 =
            UserInfo(
                houseNumber = houseNumber2,
                postcode = postcode2,
                notificationTimes = notificationTimes2,
                displayName = discordUserDisplayName2,
                discordChannelId = discordChannelId2
            )
        val notificationHandler2: DiscordNotificationHandler = mockk()
        every {
            handlerFactory.create(messageChannel = messageChannel2, userDisplayName = discordUserDisplayName2)
        } returns notificationHandler2
        every {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId1)
        } returns CompletableFuture.completedFuture(null)
        val subscriptionId2 = "test_subscriptionId2"
        every {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                houseNumber = houseNumber2,
                postcode = postcode2,
                notificationTimes = notificationTimes2,
                notificationHandler = notificationHandler2
            )
        } returns CompletableFuture.completedFuture(subscriptionId2)
        val channel2: Channel = mockk()
        every { channel2.restChannel } returns messageChannel2
        val discordClient2: GatewayDiscordClient = mockk()
        every { discordClient2.getChannelById(Snowflake.of(discordChannelId2)) } returns Mono.just(channel2)

        manager.subscribeUser(userId = userId, userInfo = userInfo2, discordClient = discordClient2)
            .get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId1)
            handlerFactory.create(messageChannel = messageChannel2, userDisplayName = discordUserDisplayName2)
            binCollectionService.subscribeToNextBinCollectionNotifications(
                houseNumber = houseNumber2,
                postcode = postcode2,
                notificationTimes = notificationTimes2,
                notificationHandler = notificationHandler2
            )
        }
        confirmVerified(binCollectionService, handlerFactory)

        // Now unsubscribe.
        clearAllMocks()

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
