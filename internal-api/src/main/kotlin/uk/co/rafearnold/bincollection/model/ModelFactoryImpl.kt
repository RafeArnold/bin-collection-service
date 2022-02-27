package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

internal class ModelFactoryImpl : ModelFactory {

    override fun createNextBinCollection(binTypes: Set<BinType>, dateOfCollection: LocalDate): NextBinCollection =
        NextBinCollectionImpl(binTypes = binTypes, dateOfCollection = dateOfCollection)

    override fun createSetUserAddressCommand(houseNumber: String, postcode: String): SetUserAddressCommand =
        SetUserAddressCommandImpl(houseNumber = houseNumber, postcode = postcode)

    override fun createAddNotificationTimeCommand(notificationTimeSetting: NotificationTimeSetting): AddNotificationTimeCommand =
        AddNotificationTimeCommandImpl(notificationTimeSetting = notificationTimeSetting)

    override fun createClearUserCommand(): ClearUserCommand = ClearUserCommandImpl

    override fun createNotificationTimeSetting(
        daysBeforeCollection: Int,
        hourOfDay: Int,
        minuteOfHour: Int
    ): NotificationTimeSetting =
        NotificationTimeSettingImpl(
            daysBeforeCollection = daysBeforeCollection,
            hourOfDay = hourOfDay,
            minuteOfHour = minuteOfHour
        )
}
