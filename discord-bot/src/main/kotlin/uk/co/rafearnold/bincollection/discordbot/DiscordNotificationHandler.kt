package uk.co.rafearnold.bincollection.discordbot

import discord4j.rest.entity.RestChannel
import uk.co.rafearnold.bincollection.Handler
import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.NextBinCollection
import java.util.concurrent.CompletableFuture

class DiscordNotificationHandler(
    private val messageChannel: RestChannel,
    private val userDisplayName: String
) : Handler<NextBinCollection> {

    override fun handle(event: NextBinCollection): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            val messageText =
                "$userDisplayName, your ${event.binTypes.joinToString(separator = " and ") { it.displayName }} bin(s) will be collected on ${event.dateOfCollection}."
            messageChannel.createMessage(messageText).block()
        }

    private val BinType.displayName
        get() =
            when (this) {
                BinType.GENERAL -> "general waste"
                BinType.RECYCLING -> "recycling"
                BinType.ORGANIC -> "organic waste"
            }
}
