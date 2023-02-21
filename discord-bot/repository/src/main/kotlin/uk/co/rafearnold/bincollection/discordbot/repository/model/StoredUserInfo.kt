package uk.co.rafearnold.bincollection.discordbot.repository.model

data class StoredUserInfo(
    var addressInfo: StoredAddressInfo,
    val notificationTimes: MutableList<StoredNotificationTimeSetting>,
    var discordUserDisplayName: String,
    var discordChannelId: String
)
