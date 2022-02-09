package uk.co.rafearnold.bincollection.messengerbot

sealed class MessengerBotServiceException(
    override val message: String,
    override val cause: Throwable?
) : Exception(message, cause)

class NoUserInfoFoundException(
    userId: String
) : MessengerBotServiceException(message = "No information found for user '$userId'", cause = null)
