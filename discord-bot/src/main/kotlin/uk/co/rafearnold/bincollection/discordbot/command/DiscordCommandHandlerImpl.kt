package uk.co.rafearnold.bincollection.discordbot.command

import discord4j.core.GatewayDiscordClient
import discord4j.rest.entity.RestChannel
import uk.co.rafearnold.bincollection.CommandParser
import uk.co.rafearnold.bincollection.discordbot.DiscordBotService
import uk.co.rafearnold.bincollection.discordbot.model.UserInfo
import uk.co.rafearnold.bincollection.model.AddNotificationTimeCommand
import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.CambridgeAddressInfo
import uk.co.rafearnold.bincollection.model.ClearUserCommand
import uk.co.rafearnold.bincollection.model.Command
import uk.co.rafearnold.bincollection.model.FremantleAddressInfo
import uk.co.rafearnold.bincollection.model.GetNextBinCollectionCommand
import uk.co.rafearnold.bincollection.model.GetUserInfoCommand
import uk.co.rafearnold.bincollection.model.HelpCommand
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import uk.co.rafearnold.bincollection.model.SetUserAddressCommand
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
                        val addressInfo: AddressInfo = cmd.addressInfo
                        botService.setUserAddress(
                            userId = userId,
                            addressInfo = addressInfo,
                            userDisplayName = userDisplayName,
                            discordChannelId = messageChannel.id.asString(),
                            discordClient = discordClient
                        )
                            .thenRun {
                                val messageText =
                                    "Address for $userDisplayName set to " +
                                            when (addressInfo) {
                                                is CambridgeAddressInfo -> "house number '${addressInfo.houseNumber}' and postcode '${addressInfo.postcode}'"
                                                is FremantleAddressInfo -> "'${addressInfo.addressQuery}'"
                                            }
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

                    is GetUserInfoCommand -> {
                        botService.loadUser(userId = userId)
                            .thenAccept { userInfo: UserInfo ->
                                val notificationsMessage: String =
                                    if (userInfo.notificationTimes.isEmpty()) "You are currently not set to receive notifications."
                                    else "You are currently set to receive notifications ${userInfo.notificationTimes.joinToString { it.buildInfoMessage() }}."
                                val messageText =
                                    "$userDisplayName, your address is " +
                                            when (userInfo.addressInfo) {
                                                is CambridgeAddressInfo -> "set to house number '${userInfo.addressInfo.houseNumber}' and postcode '${userInfo.addressInfo.postcode}'"
                                                is FremantleAddressInfo -> "set to '${userInfo.addressInfo.addressQuery}'"
                                                null -> "not set"
                                            } + ". $notificationsMessage"
                                messageChannel.createMessage(messageText).block()
                            }
                    }

                    is HelpCommand -> {
                        commandParser.getUsageText()
                            .thenAccept { messageText: String -> messageChannel.createMessage(messageText).block() }
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

    private fun NotificationTimeSetting.buildInfoMessage(): String =
        "${this.daysBeforeCollection} ${if (this.daysBeforeCollection == 1) "day" else "days"} before your next bin collection at" +
                " ${LocalTime.of(this.hourOfDay, this.minuteOfHour).format(DateTimeFormatter.ofPattern("HH:mm"))}"
}
