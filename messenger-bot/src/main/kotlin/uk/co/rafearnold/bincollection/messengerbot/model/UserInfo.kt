package uk.co.rafearnold.bincollection.messengerbot.model

import uk.co.rafearnold.bincollection.model.NotificationTimeSetting

data class UserInfo(
    val houseNumber: String,
    val postcode: String,
    val notificationTimes: Set<NotificationTimeSetting>
)
