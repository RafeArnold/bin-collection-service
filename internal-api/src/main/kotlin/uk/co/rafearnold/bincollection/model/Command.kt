package uk.co.rafearnold.bincollection.model

sealed interface Command

interface SetUserAddressCommand : Command {
    val houseNumber: String
    val postcode: String
}

interface AddNotificationTimeCommand : Command {
    val notificationTimeSetting: NotificationTimeSetting
}

interface ClearUserCommand : Command

interface GetNextBinCollectionCommand : Command
