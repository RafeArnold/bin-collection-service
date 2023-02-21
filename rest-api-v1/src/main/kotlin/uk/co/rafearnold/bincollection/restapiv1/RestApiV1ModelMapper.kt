package uk.co.rafearnold.bincollection.restapiv1

import uk.co.rafearnold.bincollection.model.AddressInfo
import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import javax.inject.Inject

class RestApiV1ModelMapper @Inject constructor(
    private val modelFactory: ModelFactory
) {

    fun mapToNotificationTimeSetting(setting: NotificationTimeSettingRestApiV1Model): NotificationTimeSetting =
        modelFactory.createNotificationTimeSetting(
            daysBeforeCollection = setting.daysBeforeCollection,
            hourOfDay = setting.hourOfDay,
            minuteOfHour = setting.minuteOfHour
        )

    fun mapToAddressInfo(addressInfo: AddressInfoRestApiV1Model): AddressInfo =
        when (addressInfo) {
            is CambridgeAddressInfoRestApiV1Model ->
                modelFactory.createCambridgeAddressInfo(
                    houseNumber = addressInfo.houseNumber,
                    postcode = addressInfo.postcode,
                )
            is FremantleAddressInfoRestApiV1Model ->
                modelFactory.createFremantleAddressInfo(addressQuery = addressInfo.addressQuery)
        }

    fun mapToNextBinCollectionRestApiV1Model(nextBinCollection: NextBinCollection): NextBinCollectionRestApiV1Model =
        NextBinCollectionRestApiV1Model(
            binTypes = nextBinCollection.binTypes.map { mapToBinTypeRestApiV1Model(it) }.toSet(),
            dateOfCollection = nextBinCollection.dateOfCollection
        )

    private fun mapToBinTypeRestApiV1Model(binType: BinType): BinTypeRestApiV1Model =
        when (binType) {
            BinType.GENERAL -> BinTypeRestApiV1Model.GENERAL
            BinType.RECYCLING -> BinTypeRestApiV1Model.RECYCLING
            BinType.ORGANIC -> BinTypeRestApiV1Model.ORGANIC
        }
}
