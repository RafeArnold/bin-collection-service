package uk.co.rafearnold.bincollection.model

data class NotificationTimeSettingImpl(
    override val daysBeforeCollection: Int,
    override val hourOfDay: Int,
    override val minuteOfHour: Int
) : NotificationTimeSetting
