package uk.co.rafearnold.bincollection.discordbot

import discord4j.core.GatewayDiscordClient
import io.mockk.Ordering
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.co.rafearnold.bincollection.AsyncLockManagerImpl
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.discordbot.model.DiscordBotModelMapper
import uk.co.rafearnold.bincollection.discordbot.model.UserInfo
import uk.co.rafearnold.bincollection.discordbot.repository.AddNotificationTimeSettingOperation
import uk.co.rafearnold.bincollection.discordbot.repository.NoSuchUserInfoFoundException
import uk.co.rafearnold.bincollection.discordbot.repository.UpdateAddressInfoOperation
import uk.co.rafearnold.bincollection.discordbot.repository.UpdateStoredUserInfoOperation
import uk.co.rafearnold.bincollection.discordbot.repository.UserInfoRepository
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredAddressInfo
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class DiscordBotServiceImplTest {

    @BeforeEach
    @AfterEach
    fun reset() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `user address can be set for a new user`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val addressInfo: AddressInfo = mockk()
        val storedAddressInfo: StoredAddressInfo = mockk()
        val discordUserDisplayName = "test_discordUserDisplayName"
        val discordChannelId = "test_discordChannelId"
        every { userInfoRepository.userInfoExists(userId = userId) } returns false
        every { modelMapper.mapToStoredAddressInfo(addressInfo = addressInfo) } returns storedAddressInfo
        val expectedStoredUserInfo =
            StoredUserInfo(
                addressInfo = storedAddressInfo,
                notificationTimes = mutableListOf(),
                discordUserDisplayName = discordUserDisplayName,
                discordChannelId = discordChannelId
            )
        every {
            userInfoRepository.createUserInfo(userId = userId, userInfo = expectedStoredUserInfo)
        } returns expectedStoredUserInfo
        val userInfo: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = expectedStoredUserInfo) } returns userInfo
        val discordClient: GatewayDiscordClient = mockk()
        every {
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        } returns CompletableFuture.completedFuture(null)

        service.setUserAddress(
            userId = userId,
            addressInfo = addressInfo,
            userDisplayName = discordUserDisplayName,
            discordChannelId = discordChannelId,
            discordClient = discordClient
        ).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.userInfoExists(userId = userId)
            modelMapper.mapToStoredAddressInfo(addressInfo = addressInfo)
            userInfoRepository.createUserInfo(userId = userId, userInfo = expectedStoredUserInfo)
            modelMapper.mapToUserInfo(storedUserInfo = expectedStoredUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user address can be set for an existing user`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val addressInfo: AddressInfo = mockk()
        val storedAddressInfo: StoredAddressInfo = mockk()
        val discordUserDisplayName = "test_discordUserDisplayName"
        val discordChannelId = "test_discordChannelId"
        every { userInfoRepository.userInfoExists(userId = userId) } returns true
        every { modelMapper.mapToStoredAddressInfo(addressInfo = addressInfo) } returns storedAddressInfo
        val expectedUpdateOperations: List<UpdateStoredUserInfoOperation> =
            listOf(UpdateAddressInfoOperation(newAddressInfo = storedAddressInfo))
        val storedUserInfo =
            StoredUserInfo(
                addressInfo = storedAddressInfo,
                notificationTimes = mutableListOf(),
                discordUserDisplayName = discordUserDisplayName,
                discordChannelId = discordChannelId
            )
        every {
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
        } returns storedUserInfo
        val userInfo: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo) } returns userInfo
        val discordClient: GatewayDiscordClient = mockk()
        every {
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        } returns CompletableFuture.completedFuture(null)

        service.setUserAddress(
            userId = userId,
            addressInfo = addressInfo,
            userDisplayName = discordUserDisplayName,
            discordChannelId = discordChannelId,
            discordClient = discordClient
        ).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.userInfoExists(userId = userId)
            modelMapper.mapToStoredAddressInfo(addressInfo = addressInfo)
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
            modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user notification time settings can be added`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val discordUserDisplayName = "test_discordUserDisplayName"
        val discordChannelId = "test_discordChannelId"
        val notificationTimeSetting: NotificationTimeSetting = mockk()
        val storedNotificationTimeSetting: StoredNotificationTimeSetting = mockk()
        every {
            modelMapper.mapToStoredNotificationTimeSetting(notificationTimeSetting = notificationTimeSetting)
        } returns storedNotificationTimeSetting
        val expectedUpdateOperations: List<UpdateStoredUserInfoOperation> =
            listOf(AddNotificationTimeSettingOperation(newNotificationTimeSetting = storedNotificationTimeSetting))
        val storedUserInfo =
            StoredUserInfo(
                addressInfo = mockk(),
                notificationTimes = mutableListOf(),
                discordUserDisplayName = discordUserDisplayName,
                discordChannelId = discordChannelId
            )
        every {
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
        } returns storedUserInfo
        val userInfo: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo) } returns userInfo
        val discordClient: GatewayDiscordClient = mockk()
        every {
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        } returns CompletableFuture.completedFuture(null)

        service.addUserNotificationTime(
            userId = userId,
            notificationTimeSetting = notificationTimeSetting,
            userDisplayName = discordUserDisplayName,
            discordChannelId = discordChannelId,
            discordClient = discordClient
        ).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            modelMapper.mapToStoredNotificationTimeSetting(notificationTimeSetting = notificationTimeSetting)
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
            modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user data can be cleared`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        every { userInfoRepository.deleteUserInfo(userId = userId) } returns mockk()
        every { subscriptionManager.unsubscribeUser(userId = userId) } returns CompletableFuture.completedFuture(null)

        service.clearUser(userId = userId).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.deleteUserInfo(userId = userId)
            subscriptionManager.unsubscribeUser(userId = userId)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user data can be loaded`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId1 = "test_userId1"
        val userId2 = "test_userId2"
        val userId3 = "test_userId3"
        val storedUserInfo1: StoredUserInfo = mockk()
        val storedUserInfo2: StoredUserInfo = mockk()
        val storedUserInfo3: StoredUserInfo = mockk()

        every {
            userInfoRepository.loadAllUserInfo()
        } returns mapOf(userId1 to storedUserInfo1, userId2 to storedUserInfo2, userId3 to storedUserInfo3)
        val userInfo1: UserInfo = mockk()
        val userInfo2: UserInfo = mockk()
        val userInfo3: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo1) } returns userInfo1
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo2) } returns userInfo2
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo3) } returns userInfo3
        val discordClient: GatewayDiscordClient = mockk()
        every {
            subscriptionManager.subscribeUser(userId = userId1, userInfo = userInfo1, discordClient = discordClient)
        } returns CompletableFuture.completedFuture(null)
        every {
            subscriptionManager.subscribeUser(userId = userId2, userInfo = userInfo2, discordClient = discordClient)
        } returns CompletableFuture.completedFuture(null)
        every {
            subscriptionManager.subscribeUser(userId = userId3, userInfo = userInfo3, discordClient = discordClient)
        } returns CompletableFuture.completedFuture(null)

        service.loadUsers(discordClient = discordClient).get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.loadAllUserInfo()
            modelMapper.mapToUserInfo(storedUserInfo1)
            subscriptionManager.subscribeUser(userId = userId1, userInfo = userInfo1, discordClient = discordClient)
            modelMapper.mapToUserInfo(storedUserInfo2)
            subscriptionManager.subscribeUser(userId = userId2, userInfo = userInfo2, discordClient = discordClient)
            modelMapper.mapToUserInfo(storedUserInfo3)
            subscriptionManager.subscribeUser(userId = userId3, userInfo = userInfo3, discordClient = discordClient)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `the next bin collection can be retrieved for an existing user`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"
        val addressInfo: AddressInfo = mockk()
        val storedAddressInfo: StoredAddressInfo = mockk()
        val userInfo =
            StoredUserInfo(
                addressInfo = storedAddressInfo,
                notificationTimes = mutableListOf(),
                discordUserDisplayName = "test_discordUserDisplayName",
                discordChannelId = "test_discordChannelId"
            )
        every { userInfoRepository.loadUserInfo(userId = userId) } returns userInfo
        every { modelMapper.mapToAddressInfo(storedAddressInfo = storedAddressInfo) } returns addressInfo
        val binCollection: NextBinCollection = mockk()
        every {
            binCollectionService.getNextBinCollection(addressInfo = addressInfo)
        } returns CompletableFuture.completedFuture(binCollection)

        val result: NextBinCollection = service.getNextBinCollection(userId = userId).get(2, TimeUnit.SECONDS)

        assertEquals(binCollection, result)
    }

    @Test
    fun `when the next bin collection is retrieved for a user that does not exist then an exception is thrown`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: DiscordBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: DiscordBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            DiscordBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"
        every { userInfoRepository.loadUserInfo(userId = userId) } returns null

        val exception: ExecutionException =
            assertThrows { service.getNextBinCollection(userId = userId).get(2, TimeUnit.SECONDS) }

        assertTrue(exception.cause is NoSuchUserInfoFoundException)
    }
}
