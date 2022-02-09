package uk.co.rafearnold.bincollection.messengerbot.handler

import uk.co.rafearnold.bincollection.messengerbot.MessengerMessageInterface
import javax.inject.Inject

class MessengerNotificationHandlerFactory @Inject constructor(
    private val messageInterface: MessengerMessageInterface
) {

    fun create(userId: String): MessengerNotificationHandler =
        MessengerNotificationHandler(userId = userId, messageInterface = messageInterface)
}
