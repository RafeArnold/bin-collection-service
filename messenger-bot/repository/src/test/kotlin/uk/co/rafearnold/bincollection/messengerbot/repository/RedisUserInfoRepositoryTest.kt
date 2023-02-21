package uk.co.rafearnold.bincollection.messengerbot.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import redis.clients.jedis.Jedis
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredCambridgeAddressInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredFremantleAddressInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

@Testcontainers
class RedisUserInfoRepositoryTest {

    companion object {

        @Container
        private val redisContainer: GenericContainer<*> =
            @Suppress("UPPER_BOUND_VIOLATED_WARNING")
            GenericContainer<GenericContainer<*>>(DockerImageName.parse("redis:5.0.3-alpine"))
                .withExposedPorts(6379)

        private lateinit var redisClient: Jedis

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            redisClient = spyk(Jedis(redisContainer.host, redisContainer.firstMappedPort))
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            redisClient.close()
        }
    }

    @BeforeEach
    @AfterEach
    fun reset() {
        redisClient.flushAll()
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `user info can be created`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_userId"
        val userInfo =
            StoredUserInfo(
                addressInfo = StoredCambridgeAddressInfo(
                    houseNumber = "test_houseNumber",
                    postcode = "test_postcode",
                ),
                notificationTimes = mutableListOf(
                    StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                    StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0)
                )
            )
        val result: StoredUserInfo = repository.createUserInfo(userId = userId, userInfo = userInfo)
        assertEquals(userInfo, result)
        val expectedRedisValue =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber","postcode":"test_postcode"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0}]}"""
        val actualRedisValue: String? =
            redisClient.get("uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId")
        assertEquals(expectedRedisValue, actualRedisValue)
    }

    @Test
    fun `when user info is already associated with the given user id then an exception is thrown and no op is performed`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_userId"
        val redisKey = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId"
        val redisValue = "any value"
        redisClient.set(redisKey, redisValue)
        val userInfo =
            StoredUserInfo(
                addressInfo = StoredCambridgeAddressInfo(
                    houseNumber = "test_houseNumber",
                    postcode = "test_postcode",
                ),
                notificationTimes = mutableListOf(
                    StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                    StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0)
                )
            )
        assertThrows<UserInfoAlreadyExistsException> { repository.createUserInfo(userId = userId, userInfo = userInfo) }
        // Verify the value has not changed in the database.
        assertEquals(redisValue, redisClient.get(redisKey))
    }

    @Test
    fun `user info can be loaded`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_userId"
        val redisValue =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber","postcode":"test_postcode"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0}]}"""
        redisClient.set("uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId", redisValue)
        val expectedUserInfo =
            StoredUserInfo(
                addressInfo = StoredCambridgeAddressInfo(
                    houseNumber = "test_houseNumber",
                    postcode = "test_postcode",
                ),
                notificationTimes = mutableListOf(
                    StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                    StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0)
                )
            )
        assertEquals(expectedUserInfo, repository.loadUserInfo(userId = userId))
    }

    @Test
    fun `when no user info exists for the requested user id then null is returned`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        assertNull(repository.loadUserInfo(userId = "test_nonExistentUserId"))
    }

    @Test
    fun `user info can be updated`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_userId"
        val redisKey = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId"
        val redisValue =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber","postcode":"test_postcode"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0}]}"""
        redisClient.set(redisKey, redisValue)

        val updateOperations1: List<UpdateStoredUserInfoOperation> =
            listOf(
                UpdateStoredUserInfoOperation {
                    it.addressInfo = StoredCambridgeAddressInfo(
                        houseNumber = "test_newHouseNumber",
                        postcode = "test_newPostcode",
                    )
                },
                UpdateStoredUserInfoOperation {
                    it.notificationTimes.add(
                        StoredNotificationTimeSetting(
                            daysBeforeCollection = 3,
                            hourOfDay = 20,
                            minuteOfHour = 30
                        )
                    )
                }
            )
        val expectedUserInfo1 =
            StoredUserInfo(
                addressInfo = StoredCambridgeAddressInfo(
                    houseNumber = "test_newHouseNumber",
                    postcode = "test_newPostcode",
                ),
                notificationTimes = mutableListOf(
                    StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                    StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0),
                    StoredNotificationTimeSetting(daysBeforeCollection = 3, hourOfDay = 20, minuteOfHour = 30)
                )
            )
        val result1: StoredUserInfo = repository.updateUserInfo(userId = userId, updateOperations = updateOperations1)
        assertEquals(expectedUserInfo1, result1)
        val expectedRedisValue1 =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_newHouseNumber","postcode":"test_newPostcode"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0},{"daysBeforeCollection":3,"hourOfDay":20,"minuteOfHour":30}]}"""
        assertEquals(expectedRedisValue1, redisClient.get(redisKey))

        val updateOperations2: List<UpdateStoredUserInfoOperation> =
            listOf(
                UpdateStoredUserInfoOperation {
                    it.addressInfo = StoredFremantleAddressInfo(addressQuery = "test_addressQuery")
                },
                UpdateStoredUserInfoOperation {
                    it.notificationTimes.add(
                        StoredNotificationTimeSetting(
                            daysBeforeCollection = 7,
                            hourOfDay = 19,
                            minuteOfHour = 45
                        )
                    )
                }
            )
        val expectedUserInfo2 =
            StoredUserInfo(
                addressInfo = StoredFremantleAddressInfo(addressQuery = "test_addressQuery"),
                notificationTimes = mutableListOf(
                    StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                    StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0),
                    StoredNotificationTimeSetting(daysBeforeCollection = 3, hourOfDay = 20, minuteOfHour = 30),
                    StoredNotificationTimeSetting(daysBeforeCollection = 7, hourOfDay = 19, minuteOfHour = 45)
                )
            )
        val result2: StoredUserInfo = repository.updateUserInfo(userId = userId, updateOperations = updateOperations2)
        assertEquals(expectedUserInfo2, result2)
        val expectedRedisValue2 =
            """{"addressInfo":{"region":"FREMANTLE","addressQuery":"test_addressQuery"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0},{"daysBeforeCollection":3,"hourOfDay":20,"minuteOfHour":30},{"daysBeforeCollection":7,"hourOfDay":19,"minuteOfHour":45}]}"""
        assertEquals(expectedRedisValue2, redisClient.get(redisKey))
    }

    @Test
    fun `when user info that does not exist is updated then an exception is thrown and no op is performed`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_nonExistentUserId"
        val redisKey = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId"

        val updateOperations: List<UpdateStoredUserInfoOperation> =
            listOf(UpdateStoredUserInfoOperation {
                it.addressInfo = StoredCambridgeAddressInfo(
                    houseNumber = "test_newHouseNumber",
                    postcode = "test_newPostcode",
                )
            })
        assertThrows<NoSuchUserInfoFoundException> {
            repository.updateUserInfo(userId = userId, updateOperations = updateOperations)
        }
        // Verify nothing was added to the database.
        assertNull(redisClient.get(redisKey))
        assertEquals(0, redisClient.dbSize())
    }

    @Test
    fun `user info can be deleted`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_userId"
        val redisKey = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId"
        val redisValue =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber","postcode":"test_postcode"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0}]}"""
        redisClient.set(redisKey, redisValue)

        val expectedUserInfo =
            StoredUserInfo(
                addressInfo = StoredCambridgeAddressInfo(
                    houseNumber = "test_houseNumber",
                    postcode = "test_postcode",
                ),
                notificationTimes = mutableListOf(
                    StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                    StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0)
                )
            )
        val result: StoredUserInfo? = repository.deleteUserInfo(userId = userId)
        assertEquals(expectedUserInfo, result)
        // Verify the value was deleted from the database.
        assertNull(redisClient.get(redisKey))
        assertEquals(0, redisClient.dbSize())
    }

    @Test
    fun `when user info that does not exist is deleted then null is returned and no op is performed`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId = "test_userId"
        val redisKey = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId"

        val result: StoredUserInfo? = repository.deleteUserInfo(userId = userId)
        assertNull(result)
        // Verify nothing was changed in the database.
        assertNull(redisClient.get(redisKey))
        assertEquals(0, redisClient.dbSize())
    }

    @Test
    fun `user info can be checked for existence`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId1 = "test_userId1"
        val userId2 = "test_userId2"
        val redisKey1 = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId1"
        redisClient.set(redisKey1, "any value")

        assertTrue(repository.userInfoExists(userId = userId1))
        assertFalse(repository.userInfoExists(userId = userId2))
    }

    @Test
    fun `all user info can be retrieved`() {
        val repository = RedisUserInfoRepository(redisClient = redisClient, objectMapper = jacksonObjectMapper())
        val userId1 = "test_userId1"
        val userId2 = "test_userId2"
        val userId3 = "test_userId3"
        val redisKey1 = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId1"
        val redisKey2 = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId2"
        val redisKey3 = "uk.co.rafearnold.bin-collection-service.messenger-bot.user-info.$userId3"

        val expectedResult1: Map<String, StoredUserInfo> = mapOf()
        val actualResult1: Map<String, StoredUserInfo> = repository.loadAllUserInfo()
        assertEquals(expectedResult1, actualResult1)

        val redisValue1 =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber1","postcode":"test_postcode1"},"notificationTimes":[{"daysBeforeCollection":4,"hourOfDay":3,"minuteOfHour":53},{"daysBeforeCollection":1,"hourOfDay":17,"minuteOfHour":0}]}"""
        redisClient.set(redisKey1, redisValue1)
        val redisValue2 =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber2","postcode":"test_postcode2"},"notificationTimes":[{"daysBeforeCollection":6,"hourOfDay":13,"minuteOfHour":30}]}"""
        redisClient.set(redisKey2, redisValue2)
        val redisValue3 =
            """{"addressInfo":{"region":"CAMBRIDGE","houseNumber":"test_houseNumber3","postcode":"test_postcode3"},"notificationTimes":[]}"""
        redisClient.set(redisKey3, redisValue3)

        val expectedResult2: Map<String, StoredUserInfo> =
            mapOf(
                userId1 to StoredUserInfo(
                    addressInfo = StoredCambridgeAddressInfo(
                        houseNumber = "test_houseNumber1",
                        postcode = "test_postcode1",
                    ),
                    notificationTimes = mutableListOf(
                        StoredNotificationTimeSetting(daysBeforeCollection = 4, hourOfDay = 3, minuteOfHour = 53),
                        StoredNotificationTimeSetting(daysBeforeCollection = 1, hourOfDay = 17, minuteOfHour = 0)
                    )
                ),
                userId2 to StoredUserInfo(
                    addressInfo = StoredCambridgeAddressInfo(
                        houseNumber = "test_houseNumber2",
                        postcode = "test_postcode2",
                    ),
                    notificationTimes = mutableListOf(
                        StoredNotificationTimeSetting(daysBeforeCollection = 6, hourOfDay = 13, minuteOfHour = 30)
                    )
                ),
                userId3 to StoredUserInfo(
                    addressInfo = StoredCambridgeAddressInfo(
                        houseNumber = "test_houseNumber3",
                        postcode = "test_postcode3",
                    ),
                    notificationTimes = mutableListOf()
                )
            )
        val actualResult2: Map<String, StoredUserInfo> = repository.loadAllUserInfo()
        assertEquals(3, actualResult2.size)
        assertEquals(expectedResult2, actualResult2)
    }
}
