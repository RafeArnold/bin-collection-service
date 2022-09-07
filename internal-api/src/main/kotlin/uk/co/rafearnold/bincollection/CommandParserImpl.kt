package uk.co.rafearnold.bincollection

import uk.co.rafearnold.bincollection.model.AddNotificationTimeCommand
import uk.co.rafearnold.bincollection.model.ClearUserCommand
import uk.co.rafearnold.bincollection.model.Command
import uk.co.rafearnold.bincollection.model.GetNextBinCollectionCommand
import uk.co.rafearnold.bincollection.model.GetUserInfoCommand
import uk.co.rafearnold.bincollection.model.HelpCommand
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import uk.co.rafearnold.bincollection.model.SetUserAddressCommand
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
                cmd.startsWith(SetUserAddressCommand.commandLiteralPrefix) -> {
                    val result: MatchResult =
                        addressRegex.matchEntire(cmd.substring(SetUserAddressCommand.commandLiteralPrefix.length))
                            ?: throw InvalidCommandException(command = command)
                    val (houseNumber: String, postcode: String) = result.destructured
                    return@supplyAsync modelFactory.createSetUserAddressCommand(
                        houseNumber = houseNumber,
                        postcode = postcode
                    )
                }
                cmd.startsWith(AddNotificationTimeCommand.commandLiteralPrefix) -> {
                    val result: MatchResult =
                        timeSettingRegex.matchEntire(cmd.substring(AddNotificationTimeCommand.commandLiteralPrefix.length))
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
                cmd == ClearUserCommand.commandLiteral -> {
                    return@supplyAsync modelFactory.createClearUserCommand()
                }
                cmd == GetNextBinCollectionCommand.commandLiteral -> {
                    return@supplyAsync modelFactory.createGetNextBinCollectionCommand()
                }
                cmd == GetUserInfoCommand.commandLiteral -> {
                    return@supplyAsync modelFactory.createGetUserInfoCommand()
                }
                cmd == HelpCommand.commandLiteral -> {
                    return@supplyAsync modelFactory.createHelpCommand()
                }
                else -> {
                    throw UnrecognisedCommandException(command = command)
                }
            }
        }

    override fun getUsageText(): CompletableFuture<String> = CompletableFuture.completedFuture(usageText)

    companion object {
        private val addressRegex = Regex("\"([\\w\\s]+)\" \"([\\w\\s]+)\"")
        private val timeSettingRegex = Regex("(\\d) (\\d{1,2}) (\\d{1,2})")

        private val usageText: String =
            """
                                |Bin collection usage:
                                |    `!${SetUserAddressCommand.commandLiteralPrefix}_addr_`
                                |       Sets the user's address.
                                |       `_addr_` must be a house number/name followed by a postcode, each surrounded by double quotes.
                                |       For example, `!${SetUserAddressCommand.commandLiteralPrefix}"236" "SW6 5GA"`.
                                |    `!${AddNotificationTimeCommand.commandLiteralPrefix}_time_`
                                |       Configures an additional notification setting. A notification will be sent to the user at the configured day and time before the user's next bin collection.
                                |       `_time_` must be three integers separated by spaces. The first integer is the number of days before the next collection that the notification will be sent. The second and third integers are the hour and minute to send the notification, respectively.
                                |       For example, `!${AddNotificationTimeCommand.commandLiteralPrefix} 1 16 0`.
                                |    `!${ClearUserCommand.commandLiteral}`
                                |       Clears all information associated with the user, including address and notification settings.
                                |    `!${GetNextBinCollectionCommand.commandLiteral}`
                                |       Returns the date of the user's next bin collection, as well as the type of bins being collected.
                                |    `!${GetUserInfoCommand.commandLiteral}`
                                |       Returns all the information currently configured for the user, including address and notification settings..
                                |    `!${HelpCommand.commandLiteral}`
                                |       Returns this usage text.
                            """.trimMargin()
    }
}
