package uk.co.rafearnold.bincollection.discordbot.repository

import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo

data class AddNotificationTimeSettingOperation(
    val newNotificationTimeSetting: StoredNotificationTimeSetting
) : UpdateStoredUserInfoOperation {
    override fun update(userInfo: StoredUserInfo) {
        userInfo.notificationTimes.add(newNotificationTimeSetting)
    }
}
