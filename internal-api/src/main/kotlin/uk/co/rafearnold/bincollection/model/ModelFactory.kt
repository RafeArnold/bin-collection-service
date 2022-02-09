package uk.co.rafearnold.bincollection.model

interface ModelFactory {

    fun createNotificationTimeSetting(
        daysBeforeCollection: Int,
        hourOfDay: Int,
        minuteOfHour: Int
    ): NotificationTimeSetting
}
