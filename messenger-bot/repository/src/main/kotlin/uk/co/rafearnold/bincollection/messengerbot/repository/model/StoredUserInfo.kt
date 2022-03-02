package uk.co.rafearnold.bincollection.messengerbot.repository.model

data class StoredUserInfo(
    var houseNumber: String,
    var postcode: String,
    val notificationTimes: MutableList<StoredNotificationTimeSetting>
)
