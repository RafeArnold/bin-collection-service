package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

internal class ModelFactoryImpl : ModelFactory {

    override fun createNextBinCollection(binTypes: Set<BinType>, dateOfCollection: LocalDate): NextBinCollection =
        NextBinCollectionImpl(binTypes = binTypes, dateOfCollection = dateOfCollection)

    override fun createCambridgeAddressInfo(houseNumber: String, postcode: String): CambridgeAddressInfo =
        CambridgeAddressInfoImpl(houseNumber = houseNumber, postcode = postcode)

    override fun createFremantleAddressInfo(addressQuery: String): FremantleAddressInfo =
        FremantleAddressInfoImpl(addressQuery = addressQuery)

    override fun createSetUserAddressCommand(addressInfo: AddressInfo): SetUserAddressCommand =
        SetUserAddressCommandImpl(addressInfo = addressInfo)

    override fun createAddNotificationTimeCommand(notificationTimeSetting: NotificationTimeSetting): AddNotificationTimeCommand =
        AddNotificationTimeCommandImpl(notificationTimeSetting = notificationTimeSetting)

    override fun createClearUserCommand(): ClearUserCommand = ClearUserCommandImpl

    override fun createGetNextBinCollectionCommand(): GetNextBinCollectionCommand = GetNextBinCollectionCommandImpl

    override fun createGetUserInfoCommand(): GetUserInfoCommand = GetUserInfoCommandImpl

    override fun createHelpCommand(): HelpCommand = HelpCommandImpl

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
