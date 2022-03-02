package uk.co.rafearnold.bincollection.discordbot.repository.model

data class StoredNotificationTimeSetting(
    val daysBeforeCollection: Int,
    val hourOfDay: Int,
    val minuteOfHour: Int
)
