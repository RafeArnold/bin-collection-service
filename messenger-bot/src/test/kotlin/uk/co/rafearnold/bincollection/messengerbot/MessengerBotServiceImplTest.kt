package uk.co.rafearnold.bincollection.messengerbot

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
import uk.co.rafearnold.bincollection.messengerbot.model.MessengerBotModelMapper
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.AddNotificationTimeSettingOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.NoSuchUserInfoFoundException
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdateHouseNumberOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdatePostcodeOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdateStoredUserInfoOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UserInfoRepository
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class MessengerBotServiceImplTest {

    @BeforeEach
    @AfterEach
    fun reset() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `user address can be set for a new user`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val postcode = "test_postcode"
        val houseNumber = "test_houseNumber"
        every { userInfoRepository.userInfoExists(userId = userId) } returns false
        val expectedStoredUserInfo =
            StoredUserInfo(houseNumber = houseNumber, postcode = postcode, notificationTimes = mutableListOf())
        every {
            userInfoRepository.createUserInfo(userId = userId, userInfo = expectedStoredUserInfo)
        } returns expectedStoredUserInfo
        val userInfo: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = expectedStoredUserInfo) } returns userInfo
        every {
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        } returns CompletableFuture.completedFuture(null)

        service.setUserAddress(userId = userId, postcode = postcode, houseNumber = houseNumber)
            .get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.userInfoExists(userId = userId)
            userInfoRepository.createUserInfo(userId = userId, userInfo = expectedStoredUserInfo)
            modelMapper.mapToUserInfo(storedUserInfo = expectedStoredUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user address can be set for an existing user`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val postcode = "test_postcode"
        val houseNumber = "test_houseNumber"
        every { userInfoRepository.userInfoExists(userId = userId) } returns true
        val expectedUpdateOperations: List<UpdateStoredUserInfoOperation> =
            listOf(
                UpdateHouseNumberOperation(newHouseNumber = houseNumber),
                UpdatePostcodeOperation(newPostcode = postcode)
            )
        val storedUserInfo =
            StoredUserInfo(houseNumber = houseNumber, postcode = postcode, notificationTimes = mutableListOf())
        every {
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
        } returns storedUserInfo
        val userInfo: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo) } returns userInfo
        every {
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        } returns CompletableFuture.completedFuture(null)

        service.setUserAddress(userId = userId, postcode = postcode, houseNumber = houseNumber)
            .get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.userInfoExists(userId = userId)
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
            modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user notification time settings can be added`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"

        val notificationTimeSetting: NotificationTimeSetting = mockk()
        val storedNotificationTimeSetting: StoredNotificationTimeSetting = mockk()
        every {
            modelMapper.mapToStoredNotificationTimeSetting(notificationTimeSetting = notificationTimeSetting)
        } returns storedNotificationTimeSetting
        val expectedUpdateOperations: List<UpdateStoredUserInfoOperation> =
            listOf(AddNotificationTimeSettingOperation(newNotificationTimeSetting = storedNotificationTimeSetting))
        val storedUserInfo =
            StoredUserInfo(
                houseNumber = "test_houseNumber",
                postcode = "test_postcode",
                notificationTimes = mutableListOf()
            )
        every {
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
        } returns storedUserInfo
        val userInfo: UserInfo = mockk()
        every { modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo) } returns userInfo
        every {
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        } returns CompletableFuture.completedFuture(null)

        service.addUserNotificationTime(userId = userId, notificationTimeSetting = notificationTimeSetting)
            .get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            modelMapper.mapToStoredNotificationTimeSetting(notificationTimeSetting = notificationTimeSetting)
            userInfoRepository.updateUserInfo(userId = userId, updateOperations = expectedUpdateOperations)
            modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `user data can be cleared`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
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
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
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
        every {
            subscriptionManager.subscribeUser(userId = userId1, userInfo = userInfo1)
        } returns CompletableFuture.completedFuture(null)
        every {
            subscriptionManager.subscribeUser(userId = userId2, userInfo = userInfo2)
        } returns CompletableFuture.completedFuture(null)
        every {
            subscriptionManager.subscribeUser(userId = userId3, userInfo = userInfo3)
        } returns CompletableFuture.completedFuture(null)

        service.loadUsers().get(2, TimeUnit.SECONDS)

        verify(ordering = Ordering.SEQUENCE) {
            userInfoRepository.loadAllUserInfo()
            modelMapper.mapToUserInfo(storedUserInfo1)
            subscriptionManager.subscribeUser(userId = userId1, userInfo = userInfo1)
            modelMapper.mapToUserInfo(storedUserInfo2)
            subscriptionManager.subscribeUser(userId = userId2, userInfo = userInfo2)
            modelMapper.mapToUserInfo(storedUserInfo3)
            subscriptionManager.subscribeUser(userId = userId3, userInfo = userInfo3)
        }
        confirmVerified(userInfoRepository, subscriptionManager, modelMapper)
    }

    @Test
    fun `the next bin collection can be retrieved for an existing user`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
                userInfoRepository = userInfoRepository,
                subscriptionManager = subscriptionManager,
                binCollectionService = binCollectionService,
                modelMapper = modelMapper,
                lockManager = lockManager
            )

        val userId = "test_userId"
        val houseNumber = "test_houseNumber"
        val postcode = "test_postcode"
        val userInfo =
            StoredUserInfo(houseNumber = houseNumber, postcode = postcode, notificationTimes = mutableListOf())
        every { userInfoRepository.loadUserInfo(userId = userId) } returns userInfo
        val binCollection: NextBinCollection = mockk()
        every {
            binCollectionService.getNextBinCollection(houseNumber = houseNumber, postcode = postcode)
        } returns CompletableFuture.completedFuture(binCollection)

        val result: NextBinCollection = service.getNextBinCollection(userId = userId).get(2, TimeUnit.SECONDS)

        assertEquals(binCollection, result)
    }

    @Test
    fun `when the next bin collection is retrieved for a user that does not exist then an exception is thrown`() {
        val userInfoRepository: UserInfoRepository = mockk()
        val subscriptionManager: MessengerBotSubscriptionManager = mockk()
        val binCollectionService: BinCollectionService = mockk()
        val modelMapper: MessengerBotModelMapper = mockk()
        val lockManager = AsyncLockManagerImpl()
        val service =
            MessengerBotServiceImpl(
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
