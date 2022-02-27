package uk.co.rafearnold.bincollection.model

import java.util.concurrent.ScheduledExecutorService

internal data class NotificationSubscriptionSettings(
    val executor: ScheduledExecutorService
)
