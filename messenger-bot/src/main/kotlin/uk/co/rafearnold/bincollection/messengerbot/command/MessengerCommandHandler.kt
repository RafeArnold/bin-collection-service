package uk.co.rafearnold.bincollection.messengerbot.command

import java.util.concurrent.CompletableFuture

interface MessengerCommandHandler {

    fun handleCommand(userId: String, command: String): CompletableFuture<Void>
}
