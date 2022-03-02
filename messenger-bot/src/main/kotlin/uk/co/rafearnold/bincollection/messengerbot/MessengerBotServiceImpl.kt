package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.AsyncLockManager
import uk.co.rafearnold.bincollection.messengerbot.model.MessengerBotModelMapper
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.AddNotificationTimeSettingOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdateHouseNumberOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdatePostcodeOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdateStoredUserInfoOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UserInfoRepository
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class MessengerBotServiceImpl @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val subscriptionManager: MessengerBotSubscriptionManager,
    private val modelMapper: MessengerBotModelMapper,
    private val lockManager: AsyncLockManager
) : MessengerBotService {

    override fun setUserAddress(
        userId: String,
        postcode: String,
        houseNumber: String
    ): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            val storedUserInfo: StoredUserInfo =
                if (!userInfoRepository.userInfoExists(userId = userId)) {
                    val storedUserInfo =
                        StoredUserInfo(
                            houseNumber = houseNumber,
                            postcode = postcode,
                            notificationTimes = mutableListOf()
                        )
                    userInfoRepository.createUserInfo(userId = userId, userInfo = storedUserInfo)
                } else {
                    val updateOperations: List<UpdateStoredUserInfoOperation> =
                        listOf(
                            UpdateHouseNumberOperation(newHouseNumber = houseNumber),
                            UpdatePostcodeOperation(newPostcode = postcode)
                        )
                    userInfoRepository.updateUserInfo(userId = userId, updateOperations = updateOperations)
                }
            val userInfo: UserInfo = modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        }

    override fun addUserNotificationTime(
        userId: String,
        notificationTimeSetting: NotificationTimeSetting
    ): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            val updateOperations: List<UpdateStoredUserInfoOperation> =
                listOf(
                    AddNotificationTimeSettingOperation(
                        newNotificationTimeSetting = modelMapper
                            .mapToStoredNotificationTimeSetting(notificationTimeSetting)
                    )
                )
            val storedUserInfo: StoredUserInfo =
                userInfoRepository.updateUserInfo(userId = userId, updateOperations = updateOperations)
            val userInfo: UserInfo = modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
        }

    override fun clearUser(userId: String): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            userInfoRepository.deleteUserInfo(userId = userId)
            subscriptionManager.unsubscribeUser(userId = userId)
        }

    override fun loadUsers(): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            val userInfoMap: Map<String, StoredUserInfo> = userInfoRepository.loadAllUserInfo()
            val subscribeFutures: List<CompletableFuture<Void>> =
                userInfoMap.map { (userId: String, storedUserInfo: StoredUserInfo) ->
                    val userInfo: UserInfo = modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
                    subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo)
                }
            CompletableFuture.allOf(*subscribeFutures.toTypedArray())
        }
}
