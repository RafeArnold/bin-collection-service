package uk.co.rafearnold.bincollection.messengerbot.repository.model

data class StoredUserInfo(
    var addressInfo: StoredAddressInfo,
    val notificationTimes: MutableList<StoredNotificationTimeSetting>
)
