package uk.co.rafearnold.bincollection.discordbot.model

import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.discordbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import javax.inject.Inject

internal class DiscordBotModelMapper @Inject constructor(
    private val modelFactory: ModelFactory
) {

    fun mapToUserInfo(storedUserInfo: StoredUserInfo): UserInfo =
        UserInfo(
            houseNumber = storedUserInfo.houseNumber,
            postcode = storedUserInfo.postcode,
            notificationTimes = storedUserInfo.notificationTimes.map { mapToNotificationTimeSetting(it) }.toSet(),
            displayName = storedUserInfo.discordUserDisplayName,
            discordChannelId = storedUserInfo.discordChannelId
        )

    private fun mapToNotificationTimeSetting(storedNotificationTimeSetting: StoredNotificationTimeSetting): NotificationTimeSetting =
        modelFactory.createNotificationTimeSetting(
            daysBeforeCollection = storedNotificationTimeSetting.daysBeforeCollection,
            hourOfDay = storedNotificationTimeSetting.hourOfDay,
            minuteOfHour = storedNotificationTimeSetting.minuteOfHour
        )

    fun mapToStoredUserInfo(userInfo: UserInfo): StoredUserInfo =
        StoredUserInfo(
            houseNumber = userInfo.houseNumber,
            postcode = userInfo.postcode,
            notificationTimes = userInfo.notificationTimes
                .map { mapToStoredNotificationTimeSetting(it) }.toMutableList(),
            discordUserDisplayName = userInfo.displayName,
            discordChannelId = userInfo.discordChannelId
        )

    fun mapToStoredNotificationTimeSetting(notificationTimeSetting: NotificationTimeSetting): StoredNotificationTimeSetting =
        StoredNotificationTimeSetting(
            daysBeforeCollection = notificationTimeSetting.daysBeforeCollection,
            hourOfDay = notificationTimeSetting.hourOfDay,
            minuteOfHour = notificationTimeSetting.minuteOfHour
        )
}
