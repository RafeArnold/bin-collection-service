package uk.co.rafearnold.bincollection.restapiv1

data class GetBinCollectionNotificationsRequestRestApiV1Model(
    val addressInfo: AddressInfoRestApiV1Model,
    val notificationTimes: Set<NotificationTimeSettingRestApiV1Model>,
)
