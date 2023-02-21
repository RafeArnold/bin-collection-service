package uk.co.rafearnold.bincollection

import uk.co.rafearnold.bincollection.model.AddNotificationTimeCommand
import uk.co.rafearnold.bincollection.model.AddressInfo
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
                    val (regionString: String, rest: String) =
                        (fullAddressRegex.matchEntire(cmd.substring(SetUserAddressCommand.commandLiteralPrefix.length))
                            ?: throw InvalidCommandException(command = command)).destructured
                    val addressInfo: AddressInfo =
                        when (regionString) {
                            cambridgeRegionIdentifier -> {
                                val result: MatchResult =
                                    cambridgeAddressRegex.matchEntire(rest)
                                        ?: throw InvalidAddressStringException(
                                            addressString = rest,
                                            regionString = regionString,
                                            command = command,
                                        )
                                val (houseNumber: String, postcode: String) = result.destructured
                                modelFactory.createCambridgeAddressInfo(houseNumber = houseNumber, postcode = postcode)
                            }

                            fremantleRegionIdentifier -> modelFactory.createFremantleAddressInfo(addressQuery = rest)

                            else -> throw UnrecognisedRegionException(regionString = regionString, command = command)
                        }
                    return@supplyAsync modelFactory.createSetUserAddressCommand(addressInfo = addressInfo)
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
        private val fullAddressRegex: Regex = Regex("(\\w+) (.*)")
        private const val cambridgeRegionIdentifier: String = "camb"
        private const val fremantleRegionIdentifier: String = "freo"
        private val cambridgeAddressRegex = Regex("\"([\\w\\s]+)\" \"([\\w\\s]+)\"")
        private val timeSettingRegex = Regex("(\\d) (\\d{1,2}) (\\d{1,2})")

        private val usageText: String =
            """
                                |Bin collection usage:
                                |    `!${SetUserAddressCommand.commandLiteralPrefix} _region_ _addr_`
                                |       Sets the user's address.
                                |       `_region_` must be a valid region. Currently, valid regions are `camb` (Cambridge, UK) and `freo` (Fremantle, WA, Australia).
                                |       `_addr_` must be formatted depending on `_region_`.
                                |       When `_region_` is set to `camb`, `_addr_` must be a house number/name followed by a postcode, each surrounded by double quotes.
                                |       For example, `!${SetUserAddressCommand.commandLiteralPrefix}camb "236" "SW6 5GA"`.
                                |       When `_region_` is set to `freo`, `_addr_` must be the first address line of the desired address.
                                |       For example, `!${SetUserAddressCommand.commandLiteralPrefix}freo 5/104 South Street`.
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
