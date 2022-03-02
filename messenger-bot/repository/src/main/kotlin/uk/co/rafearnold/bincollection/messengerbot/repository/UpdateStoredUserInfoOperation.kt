package uk.co.rafearnold.bincollection.messengerbot.repository

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

fun interface UpdateStoredUserInfoOperation {
    fun update(userInfo: StoredUserInfo)
}
