package uk.co.rafearnold.bincollection.model

import java.util.concurrent.ScheduledExecutorService

data class NotificationSubscriptionSettings(
    val executor: ScheduledExecutorService
)
