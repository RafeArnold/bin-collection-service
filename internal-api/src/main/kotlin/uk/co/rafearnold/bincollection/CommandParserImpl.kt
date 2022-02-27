package uk.co.rafearnold.bincollection

import uk.co.rafearnold.bincollection.model.Command
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class CommandParserImpl @Inject constructor(
    private val modelFactory: ModelFactory
) : CommandParser {

    override fun parseCommand(command: String): CompletableFuture<Command> =
        CompletableFuture.supplyAsync {
            if (!command.startsWith('!')) throw NotACommandException(command = command)
            val cmd: String = command.substring(1)
            when {
                cmd.startsWith("address set ") -> {
                    val result: MatchResult =
                        addressRegex.matchEntire(cmd.substring(12))
                            ?: throw InvalidCommandException(command = command)
                    val (houseNumber: String, postcode: String) = result.destructured
                    return@supplyAsync modelFactory.createSetUserAddressCommand(
                        houseNumber = houseNumber,
                        postcode = postcode
                    )
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
                    return@supplyAsync modelFactory.createAddNotificationTimeCommand(notificationTimeSetting = notificationTime)
                }
                cmd == "clear" -> {
                    return@supplyAsync modelFactory.createClearUserCommand()
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
