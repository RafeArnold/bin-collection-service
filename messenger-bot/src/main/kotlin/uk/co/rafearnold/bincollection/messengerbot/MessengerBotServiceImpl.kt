package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.AsyncLockManager
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.messengerbot.model.MessengerBotModelMapper
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.AddNotificationTimeSettingOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.NoSuchUserInfoFoundException
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdateAddressInfoOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UpdateStoredUserInfoOperation
import uk.co.rafearnold.bincollection.messengerbot.repository.UserInfoRepository
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class MessengerBotServiceImpl @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val subscriptionManager: MessengerBotSubscriptionManager,
    private val binCollectionService: BinCollectionService,
    private val modelMapper: MessengerBotModelMapper,
    private val lockManager: AsyncLockManager
) : MessengerBotService {

    override fun setUserAddress(
        userId: String,
        addressInfo: AddressInfo,
    ): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            val storedUserInfo: StoredUserInfo =
                if (!userInfoRepository.userInfoExists(userId = userId)) {
                    val storedUserInfo =
                        StoredUserInfo(
                            addressInfo = modelMapper.mapToStoredAddressInfo(addressInfo = addressInfo),
                            notificationTimes = mutableListOf()
                        )
                    userInfoRepository.createUserInfo(userId = userId, userInfo = storedUserInfo)
                } else {
                    val updateOperations: List<UpdateStoredUserInfoOperation> =
                        listOf(
                            UpdateAddressInfoOperation(
                                newAddressInfo = modelMapper.mapToStoredAddressInfo(addressInfo = addressInfo)
                            )
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

    override fun getNextBinCollection(userId: String): CompletableFuture<NextBinCollection> =
        lockManager.runAsyncWithLock {
            val userInfo: StoredUserInfo =
                userInfoRepository.loadUserInfo(userId = userId) ?: throw NoSuchUserInfoFoundException(userId = userId)
            binCollectionService.getNextBinCollection(addressInfo = modelMapper.mapToAddressInfo(storedAddressInfo = userInfo.addressInfo))
        }

    override fun loadUser(userId: String): CompletableFuture<UserInfo> =
        lockManager.runAsyncWithLock {
            val userInfo: StoredUserInfo =
                userInfoRepository.loadUserInfo(userId = userId) ?: throw NoSuchUserInfoFoundException(userId = userId)
            CompletableFuture.completedFuture(modelMapper.mapToUserInfo(storedUserInfo = userInfo))
        }
}
