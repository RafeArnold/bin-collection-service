package uk.co.rafearnold.bincollection.model

data class AddNotificationTimeCommandImpl(
    override val notificationTimeSetting: NotificationTimeSetting
) : AddNotificationTimeCommand
