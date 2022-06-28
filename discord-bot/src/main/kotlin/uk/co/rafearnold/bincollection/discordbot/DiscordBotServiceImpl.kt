package uk.co.rafearnold.bincollection.discordbot

import discord4j.core.GatewayDiscordClient
import uk.co.rafearnold.bincollection.AsyncLockManager
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.discordbot.model.DiscordBotModelMapper
import uk.co.rafearnold.bincollection.discordbot.model.UserInfo
import uk.co.rafearnold.bincollection.discordbot.repository.AddNotificationTimeSettingOperation
import uk.co.rafearnold.bincollection.discordbot.repository.NoSuchUserInfoFoundException
import uk.co.rafearnold.bincollection.discordbot.repository.UpdateHouseNumberOperation
import uk.co.rafearnold.bincollection.discordbot.repository.UpdatePostcodeOperation
import uk.co.rafearnold.bincollection.discordbot.repository.UpdateStoredUserInfoOperation
import uk.co.rafearnold.bincollection.discordbot.repository.UserInfoRepository
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class DiscordBotServiceImpl @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val subscriptionManager: DiscordBotSubscriptionManager,
    private val binCollectionService: BinCollectionService,
    private val modelMapper: DiscordBotModelMapper,
    private val lockManager: AsyncLockManager
) : DiscordBotService {

    override fun setUserAddress(
        userId: String,
        postcode: String,
        houseNumber: String,
        userDisplayName: String,
        discordChannelId: String,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            val storedUserInfo: StoredUserInfo =
                if (!userInfoRepository.userInfoExists(userId = userId)) {
                    val storedUserInfo =
                        StoredUserInfo(
                            houseNumber = houseNumber,
                            postcode = postcode,
                            notificationTimes = mutableListOf(),
                            discordUserDisplayName = userDisplayName,
                            discordChannelId = discordChannelId
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
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        }

    override fun addUserNotificationTime(
        userId: String,
        notificationTimeSetting: NotificationTimeSetting,
        userDisplayName: String,
        discordChannelId: String,
        discordClient: GatewayDiscordClient
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
            subscriptionManager.subscribeUser(userId = userId, userInfo = userInfo, discordClient = discordClient)
        }

    override fun clearUser(userId: String): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            userInfoRepository.deleteUserInfo(userId = userId)
            subscriptionManager.unsubscribeUser(userId = userId)
        }

    override fun loadUsers(discordClient: GatewayDiscordClient): CompletableFuture<Void> =
        lockManager.runAsyncWithLock {
            val userInfoMap: Map<String, StoredUserInfo> = userInfoRepository.loadAllUserInfo()
            val subscribeFutures: List<CompletableFuture<Void>> =
                userInfoMap.map { (userId: String, storedUserInfo: StoredUserInfo) ->
                    val userInfo: UserInfo = modelMapper.mapToUserInfo(storedUserInfo = storedUserInfo)
                    subscriptionManager.subscribeUser(
                        userId = userId,
                        userInfo = userInfo,
                        discordClient = discordClient
                    )
                }
            CompletableFuture.allOf(*subscribeFutures.toTypedArray())
        }

    override fun getNextBinCollection(userId: String): CompletableFuture<NextBinCollection> =
        lockManager.runAsyncWithLock {
            val userInfoMap: StoredUserInfo =
                userInfoRepository.loadUserInfo(userId = userId) ?: throw NoSuchUserInfoFoundException(userId = userId)
            binCollectionService
                .getNextBinCollection(houseNumber = userInfoMap.houseNumber, postcode = userInfoMap.postcode)
        }
}
