package uk.co.rafearnold.bincollection.messengerbot.repository.model

data class StoredNotificationTimeSetting(
    val daysBeforeCollection: Int,
    val hourOfDay: Int,
    val minuteOfHour: Int
)
