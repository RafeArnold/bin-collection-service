package uk.co.rafearnold.bincollection.messengerbot.command

import uk.co.rafearnold.bincollection.CommandParser
import uk.co.rafearnold.bincollection.CommandParserException
import uk.co.rafearnold.bincollection.InvalidAddressStringException
import uk.co.rafearnold.bincollection.InvalidCommandException
import uk.co.rafearnold.bincollection.NotACommandException
import uk.co.rafearnold.bincollection.UnrecognisedCommandException
import uk.co.rafearnold.bincollection.UnrecognisedRegionException
import uk.co.rafearnold.bincollection.messengerbot.MessengerBotService
import uk.co.rafearnold.bincollection.messengerbot.MessengerMessageInterface
import uk.co.rafearnold.bincollection.messengerbot.model.UserInfo
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

class MessengerCommandHandlerImpl @Inject constructor(
    private val commandParser: CommandParser,
    private val botService: MessengerBotService,
    private val messageInterface: MessengerMessageInterface
) : MessengerCommandHandler {

    override fun handleCommand(userId: String, command: String): CompletableFuture<Void> =
        commandParser.parseCommand(command = command)
            .exceptionally {
                val cause: Throwable? = it.cause
                if (cause is CommandParserException) {
                    when (cause) {
                        is InvalidCommandException -> {
                            val messageText = "Invalid command. Try `!help`"
                            messageInterface.sendMessage(userId = userId, messageText = messageText)
                        }

                        is NotACommandException -> {
                            val messageText = "Not a command. Try `!help`"
                            messageInterface.sendMessage(userId = userId, messageText = messageText)
                        }

                        is UnrecognisedCommandException -> {
                            val messageText = "Unrecognised command. Try `!help`"
                            messageInterface.sendMessage(userId = userId, messageText = messageText)
                        }

                        is InvalidAddressStringException -> {
                            val messageText =
                                "Invalid address format '${cause.addressString}' for region '${cause.regionString}'. Try `!help`"
                            messageInterface.sendMessage(userId = userId, messageText = messageText)
                        }

                        is UnrecognisedRegionException -> {
                            val messageText = "Unrecognised region '${cause.regionString}'. Try `!help`"
                            messageInterface.sendMessage(userId = userId, messageText = messageText)
                        }
                    }
                }
                throw it
            }
            .thenCompose { cmd: Command ->
                when (cmd) {
                    is SetUserAddressCommand -> {
                        val addressInfo: AddressInfo = cmd.addressInfo
                        botService.setUserAddress(userId = userId, addressInfo = addressInfo)
                            .thenRun {
                                val messageText =
                                    "Address set to " +
                                            when (addressInfo) {
                                                is CambridgeAddressInfo -> "house number '${addressInfo.houseNumber}' and postcode '${addressInfo.postcode}' in Cambridge region"
                                                is FremantleAddressInfo -> "'${addressInfo.addressQuery}' in Fremantle region"
                                            }
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
                                    "Your address is " +
                                            when (userInfo.addressInfo) {
                                                is CambridgeAddressInfo -> "set to house number '${userInfo.addressInfo.houseNumber}' and postcode '${userInfo.addressInfo.postcode}'"
                                                is FremantleAddressInfo -> "set to '${userInfo.addressInfo.addressQuery}'"
                                                null -> "not set"
                                            } + ". $notificationsMessage"
                                messageInterface.sendMessage(userId = userId, messageText = messageText)
                            }
                    }

                    is HelpCommand -> {
                        commandParser.getUsageText()
                            .thenAccept { messageText: String ->
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
                " ${LocalTime.of(this.hourOfDay, this.minuteOfHour).format(DateTimeFormatter.ofPattern("HH:mm"))}"
}
