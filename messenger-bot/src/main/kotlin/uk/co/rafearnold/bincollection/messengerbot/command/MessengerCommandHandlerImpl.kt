package uk.co.rafearnold.bincollection.messengerbot.command

import uk.co.rafearnold.bincollection.CommandParser
import uk.co.rafearnold.bincollection.messengerbot.MessengerBotService
import uk.co.rafearnold.bincollection.messengerbot.MessengerMessageInterface
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
import uk.co.rafearnold.bincollection.model.AddNotificationTimeCommand
import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.ClearUserCommand
import uk.co.rafearnold.bincollection.model.Command
import uk.co.rafearnold.bincollection.model.GetNextBinCollectionCommand
import uk.co.rafearnold.bincollection.model.GetUserInfoCommand
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import uk.co.rafearnold.bincollection.model.SetUserAddressCommand
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class MessengerCommandHandlerImpl @Inject constructor(
    private val commandParser: CommandParser,
    private val botService: MessengerBotService,
    private val messageInterface: MessengerMessageInterface
) : MessengerCommandHandler {

    override fun handleCommand(userId: String, command: String): CompletableFuture<Void> =
        commandParser.parseCommand(command = command)
            .thenCompose { cmd: Command ->
                when (cmd) {
                    is SetUserAddressCommand -> {
                        botService.setUserAddress(
                            userId = userId,
                            postcode = cmd.postcode,
                            houseNumber = cmd.houseNumber
                        )
                            .thenRun {
                                val messageText =
                                    "Address set to house number '${cmd.houseNumber}' and postcode '${cmd.postcode}'"
                                messageInterface.sendMessage(userId = userId, messageText = messageText)
                            }
                    }
                    is AddNotificationTimeCommand -> {
                        botService.addUserNotificationTime(
                            userId = userId,
                            notificationTimeSetting = cmd.notificationTimeSetting
                        )
                            .thenRun {
                                val setting: NotificationTimeSetting = cmd.notificationTimeSetting
                                val notificationTime: LocalTime = LocalTime.of(setting.hourOfDay, setting.minuteOfHour)
                                val messageText =
                                    "New notification time added. An alert will be sent ${setting.daysBeforeCollection} day(s) before each bin collection at $notificationTime"
                                messageInterface.sendMessage(userId = userId, messageText = messageText)
                            }
                    }
                    is ClearUserCommand -> {
                        botService.clearUser(userId = userId)
                            .thenRun {
                                val messageText = "User data cleared"
                                messageInterface.sendMessage(userId = userId, messageText = messageText)
                            }
                    }
                    is GetNextBinCollectionCommand -> {
                        botService.getNextBinCollection(userId = userId)
                            .thenAccept { nextBinCollection: NextBinCollection ->
                                val messageText =
                                    "Your ${nextBinCollection.binTypes.joinToString(separator = " and ") { it.displayName }} bin(s) will be collected on ${nextBinCollection.dateOfCollection}."
                                messageInterface.sendMessage(userId = userId, messageText = messageText)
                            }
                    }
                    is GetUserInfoCommand -> {
                        botService.loadUser(userId = userId)
                            .thenAccept { userInfo: UserInfo ->
                                val notificationsMessage: String =
                                    if (userInfo.notificationTimes.isEmpty()) "You are currently not set to receive notifications."
                                    else "You are currently set to receive notifications ${userInfo.notificationTimes.joinToString { it.buildInfoMessage() }}."
                                val messageText =
                                    "Your address is set to \"${userInfo.houseNumber}\" \"${userInfo.postcode}\". $notificationsMessage"
                                messageInterface.sendMessage(userId = userId, messageText = messageText)
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

    private fun NotificationTimeSetting.buildInfoMessage(): String =
        "${this.daysBeforeCollection} ${if (this.daysBeforeCollection == 1) "day" else "days"} before your next bin collection at" +
                " ${LocalTime.of(this.hourOfDay, this.minuteOfHour).format(DateTimeFormatter.ofPattern("hh:mm"))}"
}
