package uk.co.rafearnold.bincollection.discordbot.repository

import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredAddressInfo
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo

data class UpdateAddressInfoOperation(
    val newAddressInfo: StoredAddressInfo,
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.addressInfo = newAddressInfo
    }
}
