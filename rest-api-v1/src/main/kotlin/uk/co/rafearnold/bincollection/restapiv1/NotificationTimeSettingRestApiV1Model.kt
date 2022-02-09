package uk.co.rafearnold.bincollection.restapiv1

data class NotificationTimeSettingRestApiV1Model(
    val daysBeforeCollection: Int,
    val hourOfDay: Int,
    val minuteOfHour: Int
)
