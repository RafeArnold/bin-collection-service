package uk.co.rafearnold.bincollection.messengerbot.repository

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredAddressInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

data class UpdateAddressInfoOperation(
    val newAddressInfo: StoredAddressInfo
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.addressInfo = newAddressInfo
    }
}
