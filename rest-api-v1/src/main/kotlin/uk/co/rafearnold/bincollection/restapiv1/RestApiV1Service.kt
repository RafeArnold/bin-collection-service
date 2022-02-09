package uk.co.rafearnold.bincollection.restapiv1

import uk.co.rafearnold.bincollection.Handler
import java.util.concurrent.CompletableFuture

interface RestApiV1Service {

    fun getBinCollectionNotifications(
        houseNumber: String,
        postcode: String,
        notificationTimes: Set<NotificationTimeSettingRestApiV1Model>,
        notificationHandler: Handler<NextBinCollectionRestApiV1Model>
    ): CompletableFuture<String>

    fun endGetBinCollectionNotifications(subscriptionId: String): CompletableFuture<Void>
}
