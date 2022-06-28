package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

interface ModelFactory {

    fun createNextBinCollection(binTypes: Set<BinType>, dateOfCollection: LocalDate): NextBinCollection

    fun createSetUserAddressCommand(houseNumber: String, postcode: String): SetUserAddressCommand

    fun createAddNotificationTimeCommand(notificationTimeSetting: NotificationTimeSetting): AddNotificationTimeCommand

    fun createClearUserCommand(): ClearUserCommand

    fun createGetNextBinCollectionCommand(): GetNextBinCollectionCommand

    fun createNotificationTimeSetting(
        daysBeforeCollection: Int,
        hourOfDay: Int,
        minuteOfHour: Int
    ): NotificationTimeSetting
}
