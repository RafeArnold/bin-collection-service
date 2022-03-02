package uk.co.rafearnold.bincollection.messengerbot.repository

sealed class UserInfoRepositoryException(override val message: String) : Exception(message)

class UserInfoAlreadyExistsException(
    userId: String
) : UserInfoRepositoryException(message = "User info with ID '$userId' already exists")

class NoSuchUserInfoFoundException(
    userId: String
) : UserInfoRepositoryException(message = "No user info with ID '$userId' exists")
