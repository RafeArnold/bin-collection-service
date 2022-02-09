package uk.co.rafearnold.bincollection.messengerbot

import java.util.concurrent.CompletableFuture

interface MessengerMessageInterface {

    fun sendMessage(userId: String, messageText: String): CompletableFuture<Void>
}
