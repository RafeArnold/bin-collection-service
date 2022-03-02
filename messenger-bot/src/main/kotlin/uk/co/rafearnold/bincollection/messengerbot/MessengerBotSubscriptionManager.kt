package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import java.util.concurrent.CompletableFuture

interface MessengerBotSubscriptionManager {

    fun subscribeUser(userId: String, userInfo: UserInfo): CompletableFuture<Void>

    fun unsubscribeUser(userId: String): CompletableFuture<Void>
}
