package uk.co.rafearnold.bincollection.discordbot.command

import discord4j.rest.entity.RestChannel
import uk.co.rafearnold.bincollection.CommandParser
import uk.co.rafearnold.bincollection.discordbot.DiscordBotService
import uk.co.rafearnold.bincollection.model.AddNotificationTimeCommand
import uk.co.rafearnold.bincollection.model.ClearUserCommand
import uk.co.rafearnold.bincollection.model.Command
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
        userDisplayName: String
    ): CompletableFuture<Void> =
        commandParser.parseCommand(command = command)
            .thenCompose { cmd: Command ->
                when (cmd) {
                    is SetUserAddressCommand -> {
                        botService.setUserAddress(
                            userId = userId,
                            postcode = cmd.postcode,
                            houseNumber = cmd.houseNumber,
                            messageChannel = messageChannel,
                            userDisplayName = userDisplayName
                        )
                            .thenRun {
                                val messageText =
                                    "Address set to house number '${cmd.houseNumber}' and postcode '${cmd.postcode}'"
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                    is AddNotificationTimeCommand -> {
                        botService.addUserNotificationTime(
                            userId = userId,
                            notificationTimeSetting = cmd.notificationTimeSetting,
                            messageChannel = messageChannel,
                            userDisplayName = userDisplayName
                        )
                            .thenRun {
                                val setting: NotificationTimeSetting = cmd.notificationTimeSetting
                                val notificationTime: LocalTime = LocalTime.of(setting.hourOfDay, setting.minuteOfHour)
                                val messageText =
                                    "New notification time added. An alert will be sent ${setting.daysBeforeCollection} day(s) before each bin collection at $notificationTime"
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                    is ClearUserCommand -> {
                        botService.clearUser(userId = userId)
                            .thenRun {
                                val messageText = "User data cleared"
                                messageChannel.createMessage(messageText).block()
                            }
                    }
                }
            }
}