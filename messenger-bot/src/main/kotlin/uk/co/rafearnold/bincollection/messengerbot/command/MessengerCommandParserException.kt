package uk.co.rafearnold.bincollection.messengerbot.command

sealed class MessengerCommandParserException(
    override val message: String,
    override val cause: Throwable?
) : Exception(message, cause)

class NotACommandException(
    command: String
) : MessengerCommandParserException(message = "'$command' is not a command", cause = null)

class UnrecognisedCommandException(
    command: String
) : MessengerCommandParserException(message = "Unrecognised command '$command'", cause = null)

class InvalidCommandException(
    command: String
) : MessengerCommandParserException(message = "Invalid command '$command'", cause = null)
