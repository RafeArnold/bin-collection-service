package uk.co.rafearnold.bincollection.discordbot.repository

import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo

data class UpdateHouseNumberOperation(
    val newHouseNumber: String
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.houseNumber = newHouseNumber
    }
}
