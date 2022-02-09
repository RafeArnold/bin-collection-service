package uk.co.rafearnold.bincollection.messengerbot

import uk.co.rafearnold.bincollection.model.NotificationTimeSetting

data class UserInfo(
    var subscriptionId: String?,
    var houseNumber: String,
    var postcode: String,
    val notificationTimes: MutableSet<NotificationTimeSetting>
)
