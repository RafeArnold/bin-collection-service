package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

interface ModelFactory {

    fun createNextBinCollection(binTypes: Set<BinType>, dateOfCollection: LocalDate): NextBinCollection

    fun createCambridgeAddressInfo(houseNumber: String, postcode: String): CambridgeAddressInfo

    fun createFremantleAddressInfo(addressQuery: String): FremantleAddressInfo

    fun createSetUserAddressCommand(addressInfo: AddressInfo): SetUserAddressCommand

    fun createAddNotificationTimeCommand(notificationTimeSetting: NotificationTimeSetting): AddNotificationTimeCommand

    fun createClearUserCommand(): ClearUserCommand

    fun createGetNextBinCollectionCommand(): GetNextBinCollectionCommand

    fun createGetUserInfoCommand(): GetUserInfoCommand

    fun createHelpCommand(): HelpCommand

    fun createNotificationTimeSetting(
        daysBeforeCollection: Int,
        hourOfDay: Int,
        minuteOfHour: Int
    ): NotificationTimeSetting
}
