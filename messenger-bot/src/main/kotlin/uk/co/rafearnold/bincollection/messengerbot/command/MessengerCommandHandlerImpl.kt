package uk.co.rafearnold.bincollection.messengerbot.command

import uk.co.rafearnold.bincollection.messengerbot.MessengerBotService
import uk.co.rafearnold.bincollection.messengerbot.MessengerMessageInterface
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.time.LocalTime
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class MessengerCommandHandlerImpl @Inject constructor(
    private val commandParser: MessengerCommandParser,
    private val botService: MessengerBotService,
    private val messageInterface: MessengerMessageInterface
) : MessengerCommandHandler {

    override fun handleCommand(userId: String, command: String): CompletableFuture<Void> =
        commandParser.parseCommand(command = command)
            .thenCompose { cmd: MessengerCommand ->
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
                }
            }
}
