package uk.co.rafearnold.bincollection.messengerbot.repository

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo

data class AddNotificationTimeSettingOperation(
    val newNotificationTimeSetting: StoredNotificationTimeSetting
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.notificationTimes.add(newNotificationTimeSetting)
    }
}
