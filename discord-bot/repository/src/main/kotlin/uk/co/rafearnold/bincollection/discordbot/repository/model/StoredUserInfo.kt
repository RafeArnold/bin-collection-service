package uk.co.rafearnold.bincollection.discordbot.repository.model

data class StoredUserInfo(
    var houseNumber: String,
    var postcode: String,
    val notificationTimes: MutableList<StoredNotificationTimeSetting>,
    var discordUserDisplayName: String,
    var discordChannelId: String
)
