package uk.co.rafearnold.bincollection.messengerbot.handler

import uk.co.rafearnold.bincollection.Handler
import uk.co.rafearnold.bincollection.messengerbot.MessengerMessageInterface
import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.NextBinCollection
import java.util.concurrent.CompletableFuture

class MessengerNotificationHandler(
    private val userId: String,
    private val messageInterface: MessengerMessageInterface
) : Handler<NextBinCollection> {

    override fun handle(event: NextBinCollection): CompletableFuture<Void> =
        CompletableFuture.completedFuture(null).thenCompose {
            val messageText =
                "Your ${event.binTypes.joinToString(separator = " and ") { it.displayName }} bin(s) will be collected on ${event.dateOfCollection}."
            messageInterface.sendMessage(userId = userId, messageText = messageText)
        }

    private val BinType.displayName
        get() =
            when (this) {
                BinType.GENERAL -> "general waste"
                BinType.RECYCLING -> "recycling"
                BinType.ORGANIC -> "organic waste"
            }
}
