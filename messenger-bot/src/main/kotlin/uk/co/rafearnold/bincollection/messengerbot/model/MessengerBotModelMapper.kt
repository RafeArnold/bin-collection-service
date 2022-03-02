package uk.co.rafearnold.bincollection.messengerbot.model

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import javax.inject.Inject

internal class MessengerBotModelMapper @Inject constructor(
    private val modelFactory: ModelFactory
) {

    fun mapToUserInfo(storedUserInfo: StoredUserInfo): UserInfo =
        UserInfo(
            houseNumber = storedUserInfo.houseNumber,
            postcode = storedUserInfo.postcode,
            notificationTimes = storedUserInfo.notificationTimes.map { mapToNotificationTimeSetting(it) }.toSet()
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
                .map { mapToStoredNotificationTimeSetting(it) }.toMutableList()
        )

    fun mapToStoredNotificationTimeSetting(notificationTimeSetting: NotificationTimeSetting): StoredNotificationTimeSetting =
        StoredNotificationTimeSetting(
            daysBeforeCollection = notificationTimeSetting.daysBeforeCollection,
            hourOfDay = notificationTimeSetting.hourOfDay,
            minuteOfHour = notificationTimeSetting.minuteOfHour
        )
}
