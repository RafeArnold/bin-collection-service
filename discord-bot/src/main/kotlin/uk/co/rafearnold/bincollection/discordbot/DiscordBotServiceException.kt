package uk.co.rafearnold.bincollection.discordbot

sealed class DiscordBotServiceException(
    override val message: String,
    override val cause: Throwable?
) : Exception(message, cause)

class NoUserInfoFoundException(
    userId: String
) : DiscordBotServiceException(message = "No information found for user '$userId'", cause = null)
