package uk.co.rafearnold.bincollection.discordbot.repository

import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo

fun interface UpdateStoredUserInfoOperation {
    fun update(userInfo: StoredUserInfo)
}
