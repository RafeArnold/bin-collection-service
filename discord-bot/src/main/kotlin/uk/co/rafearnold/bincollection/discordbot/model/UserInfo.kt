package uk.co.rafearnold.bincollection.discordbot.model

import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting

data class UserInfo(
    val addressInfo: AddressInfo?,
    val notificationTimes: Set<NotificationTimeSetting>,
    val displayName: String,
    val discordChannelId: String
)
