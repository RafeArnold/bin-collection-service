package uk.co.rafearnold.bincollection.messengerbot.repository

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

data class UpdateHouseNumberOperation(
    val newHouseNumber: String
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.houseNumber = newHouseNumber
    }
}
