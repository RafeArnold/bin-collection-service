package uk.co.rafearnold.bincollection.messengerbot.model

import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting

data class UserInfo(
    val addressInfo: AddressInfo?,
    val notificationTimes: Set<NotificationTimeSetting>
)
