package uk.co.rafearnold.bincollection.discordbot.command

import discord4j.core.GatewayDiscordClient
import discord4j.rest.entity.RestChannel
import uk.co.rafearnold.bincollection.CommandParser
import uk.co.rafearnold.bincollection.discordbot.DiscordBotService
import uk.co.rafearnold.bincollection.model.AddNotificationTimeCommand
import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.ClearUserCommand
import uk.co.rafearnold.bincollection.model.Command
import uk.co.rafearnold.bincollection.model.GetNextBinCollectionCommand
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import uk.co.rafearnold.bincollection.model.SetUserAddressCommand
import java.time.LocalTime
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class DiscordCommandHandlerImpl @Inject constructor(
    private val commandParser: CommandParser,
    private val botService: DiscordBotService
) : DiscordCommandHandler {

    override fun handleCommand(
        userId: String,
        command: String,
        messageChannel: RestChannel,
        userDisplayName: String,
        discordClient: GatewayDiscordClient
    ): CompletableFuture<Void> =
        commandParser.parseCommand(command = command)
            .thenCompose { cmd: Command ->
                when (cmd) {
                    is SetUserAddressCommand -> {
                        botService.setUserAddress(
                            userId = userId,
                            postcode = cmd.postcode,
                            houseNumber = cmd.houseNumber,
                            userDisplayName = userDisplayName,
                            discordChannelId = messageChannel.id.asString(),
                            discordClient = discordClient
                        )
                            .thenRun {
                                val messageText =
                                    "Address for $userDisplayName set to house number '${cmd.houseNumber}' and postcode '${cmd.postcode}'"
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                    is AddNotificationTimeCommand -> {
                        botService.addUserNotificationTime(
                            userId = userId,
                            notificationTimeSetting = cmd.notificationTimeSetting,
                            userDisplayName = userDisplayName,
                            discordChannelId = messageChannel.id.asString(),
                            discordClient = discordClient
                        )
                            .thenRun {
                                val setting: NotificationTimeSetting = cmd.notificationTimeSetting
                                val notificationTime: LocalTime = LocalTime.of(setting.hourOfDay, setting.minuteOfHour)
                                val messageText =
                                    "New notification time added for $userDisplayName. An alert will be sent ${setting.daysBeforeCollection} day(s) before each bin collection at $notificationTime"
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                    is ClearUserCommand -> {
                        botService.clearUser(userId = userId)
                            .thenRun {
                                val messageText = "User data cleared for $userDisplayName"
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                    is GetNextBinCollectionCommand -> {
                        botService.getNextBinCollection(userId = userId)
                            .thenAccept { nextBinCollection: NextBinCollection ->
                                val messageText =
                                    "$userDisplayName, your ${nextBinCollection.binTypes.joinToString(separator = " and ") { it.displayName }} bin(s) will be collected on ${nextBinCollection.dateOfCollection}."
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                }
            }

    private val BinType.displayName
        get() =
            when (this) {
                BinType.GENERAL -> "general waste"
                BinType.RECYCLING -> "recycling"
                BinType.ORGANIC -> "organic waste"
            }
}
