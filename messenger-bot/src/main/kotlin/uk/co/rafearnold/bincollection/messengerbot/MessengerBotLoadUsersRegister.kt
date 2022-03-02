package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.Register
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class MessengerBotLoadUsersRegister @Inject constructor(
    private val botService: MessengerBotService
) : Register {

    override fun register(): CompletableFuture<Void> = botService.loadUsers()
}
