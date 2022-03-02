package uk.co.rafearnold.bincollection.messengerbot.repository

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

data class UpdatePostcodeOperation(
    val newPostcode: String
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.postcode = newPostcode
    }
}
