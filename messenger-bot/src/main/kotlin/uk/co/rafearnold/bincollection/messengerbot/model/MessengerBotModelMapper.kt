package uk.co.rafearnold.bincollection.messengerbot.model

import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredAddressInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredCambridgeAddressInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredFremantleAddressInfo
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredNotificationTimeSetting
import uk.co.rafearnold.bincollection.messengerbot.repository.model.StoredUserInfo
import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.CambridgeAddressInfo
import uk.co.rafearnold.bincollection.model.FremantleAddressInfo
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import javax.inject.Inject

internal class MessengerBotModelMapper @Inject constructor(
    private val modelFactory: ModelFactory
) {

    fun mapToUserInfo(storedUserInfo: StoredUserInfo): UserInfo =
        UserInfo(
            addressInfo = mapToAddressInfo(storedAddressInfo = storedUserInfo.addressInfo),
            notificationTimes = storedUserInfo.notificationTimes.map { mapToNotificationTimeSetting(it) }.toSet()
        )

    private fun mapToNotificationTimeSetting(storedNotificationTimeSetting: StoredNotificationTimeSetting): NotificationTimeSetting =
        modelFactory.createNotificationTimeSetting(
            daysBeforeCollection = storedNotificationTimeSetting.daysBeforeCollection,
            hourOfDay = storedNotificationTimeSetting.hourOfDay,
            minuteOfHour = storedNotificationTimeSetting.minuteOfHour
        )

    fun mapToAddressInfo(storedAddressInfo: StoredAddressInfo): AddressInfo =
        when (storedAddressInfo) {
            is StoredCambridgeAddressInfo ->
                modelFactory.createCambridgeAddressInfo(
                    houseNumber = storedAddressInfo.houseNumber,
                    postcode = storedAddressInfo.postcode,
                )

            is StoredFremantleAddressInfo ->
                modelFactory.createFremantleAddressInfo(addressQuery = storedAddressInfo.addressQuery)
        }

    fun mapToStoredNotificationTimeSetting(notificationTimeSetting: NotificationTimeSetting): StoredNotificationTimeSetting =
        StoredNotificationTimeSetting(
            daysBeforeCollection = notificationTimeSetting.daysBeforeCollection,
            hourOfDay = notificationTimeSetting.hourOfDay,
            minuteOfHour = notificationTimeSetting.minuteOfHour
        )

    fun mapToStoredAddressInfo(addressInfo: AddressInfo): StoredAddressInfo =
        when (addressInfo) {
            is CambridgeAddressInfo -> StoredCambridgeAddressInfo(
                houseNumber = addressInfo.houseNumber,
                postcode = addressInfo.postcode,
            )

            is FremantleAddressInfo -> StoredFremantleAddressInfo(addressQuery = addressInfo.addressQuery)
        }
}
