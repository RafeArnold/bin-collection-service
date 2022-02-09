package uk.co.rafearnold.bincollection.model

interface NotificationTimeSetting {
    val daysBeforeCollection: Int
    val hourOfDay: Int
    val minuteOfHour: Int
}
