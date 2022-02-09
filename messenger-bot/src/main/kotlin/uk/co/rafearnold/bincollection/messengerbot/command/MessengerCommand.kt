package uk.co.rafearnold.bincollection.messengerbot.command

import uk.co.rafearnold.bincollection.model.NotificationTimeSetting

sealed interface MessengerCommand

data class SetUserAddressCommand(
    val houseNumber: String,
    val postcode: String
) : MessengerCommand

data class AddNotificationTimeCommand(
    val notificationTimeSetting: NotificationTimeSetting
) : MessengerCommand

object ClearUserCommand : MessengerCommand
