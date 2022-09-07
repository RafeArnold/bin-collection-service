package uk.co.rafearnold.bincollection.model

sealed interface Command

interface SetUserAddressCommand : Command {
    val houseNumber: String
    val postcode: String

    companion object {
        const val commandLiteralPrefix: String = "address set "
    }
}

interface AddNotificationTimeCommand : Command {
    val notificationTimeSetting: NotificationTimeSetting

    companion object {
        const val commandLiteralPrefix: String = "notif add "
    }
}

interface ClearUserCommand : Command {
    companion object {
        const val commandLiteral: String = "clear"
    }
}

interface GetNextBinCollectionCommand : Command {
    companion object {
        const val commandLiteral: String = "next"
    }
}

interface GetUserInfoCommand : Command {
    companion object {
        const val commandLiteral: String = "info"
    }
}

interface HelpCommand : Command {
    companion object {
        const val commandLiteral: String = "help"
    }
}
