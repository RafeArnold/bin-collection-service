package uk.co.rafearnold.bincollection

sealed class CommandParserException(
    override val message: String,
    override val cause: Throwable?
) : Exception(message, cause)

class NotACommandException(
    command: String
) : CommandParserException(message = "'$command' is not a command", cause = null)

class UnrecognisedCommandException(
    command: String
) : CommandParserException(message = "Unrecognised command '$command'", cause = null)

class InvalidCommandException(
    command: String
) : CommandParserException(message = "Invalid command '$command'", cause = null)

class InvalidAddressStringException(
    val addressString: String,
    val regionString: String,
    command: String,
) : CommandParserException(
    message = "Invalid address string '$addressString' for region '$regionString' in '$command'",
    cause = null,
)

class UnrecognisedRegionException(
    val regionString: String,
    command: String,
) : CommandParserException(message = "Unrecognised region '$regionString' in '$command'", cause = null)
