package uk.co.rafearnold.bincollection.model

internal data class AddNotificationTimeCommandImpl(
    override val notificationTimeSetting: NotificationTimeSetting
) : AddNotificationTimeCommand
