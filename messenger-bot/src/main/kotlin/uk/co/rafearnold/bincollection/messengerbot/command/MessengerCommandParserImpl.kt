package uk.co.rafearnold.bincollection.messengerbot.command

import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class MessengerCommandParserImpl @Inject constructor(
    private val modelFactory: ModelFactory
) : MessengerCommandParser {

    override fun parseCommand(command: String): CompletableFuture<MessengerCommand> =
        CompletableFuture.supplyAsync {
            if (!command.startsWith('!')) throw NotACommandException(command = command)
            val cmd: String = command.substring(1)
            when {
                cmd.startsWith("address set ") -> {
                    val result: MatchResult =
                        addressRegex.matchEntire(cmd.substring(12))
                            ?: throw InvalidCommandException(command = command)
                    val (houseNumber: String, postcode: String) = result.destructured
                    return@supplyAsync SetUserAddressCommand(houseNumber = houseNumber, postcode = postcode)
                }
                cmd.startsWith("notif add ") -> {
                    val result: MatchResult =
                        timeSettingRegex.matchEntire(cmd.substring(10))
                            ?: throw InvalidCommandException(command = command)
                    val (daysBeforeCollectionString: String, hourOfDayString: String, minuteOfHourString: String) = result.destructured
                    val daysBeforeCollection: Int =
                        daysBeforeCollectionString.toInt()
                            .also { if (it < 1 || it > 7) throw InvalidCommandException(command = command) }
                    val hourOfDay: Int =
                        hourOfDayString.toInt()
                            .also { if (it < 0 || it > 23) throw InvalidCommandException(command = command) }
                    val minuteOfHour: Int =
                        minuteOfHourString.toInt()
                            .also { if (it < 0 || it > 59) throw InvalidCommandException(command = command) }
                    val notificationTime: NotificationTimeSetting =
                        modelFactory.createNotificationTimeSetting(
                            daysBeforeCollection = daysBeforeCollection,
                            hourOfDay = hourOfDay,
                            minuteOfHour = minuteOfHour
                        )
                    return@supplyAsync AddNotificationTimeCommand(notificationTimeSetting = notificationTime)
                }
                cmd == "clear" -> {
                    return@supplyAsync ClearUserCommand
                }
                else -> {
                    throw UnrecognisedCommandException(command = command)
                }
            }
        }

    companion object {
        private val addressRegex = Regex("\"([\\w\\s]+)\" \"([\\w\\s]+)\"")
        private val timeSettingRegex = Regex("(\\d) (\\d{1,2}) (\\d{1,2})")
    }
}
