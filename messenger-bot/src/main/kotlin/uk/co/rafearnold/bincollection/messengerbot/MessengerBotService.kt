package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture

interface MessengerBotService {

    fun setUserAddress(
        userId: String,
        postcode: String,
        houseNumber: String
    ): CompletableFuture<Void>

    fun addUserNotificationTime(
        userId: String,
        notificationTimeSetting: NotificationTimeSetting
    ): CompletableFuture<Void>

    fun clearUser(userId: String): CompletableFuture<Void>

    fun loadUsers(): CompletableFuture<Void>

    fun getNextBinCollection(userId: String): CompletableFuture<NextBinCollection>

    fun loadUser(userId: String): CompletableFuture<UserInfo>
}
