package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

class ModelFactoryImpl : BackendModelFactory {

    override fun createNextBinCollection(binTypes: Set<BinType>, dateOfCollection: LocalDate): NextBinCollection =
        NextBinCollectionImpl(binTypes = binTypes, dateOfCollection = dateOfCollection)

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
