package uk.co.rafearnold.bincollection.restapiv1

import uk.co.rafearnold.bincollection.Handler
import java.util.concurrent.CompletableFuture

interface RestApiV1Service {

    fun getNextBinCollection(
        request: GetNextBinCollectionRequestRestApiV1Model,
    ): CompletableFuture<GetNextBinCollectionResponseRestApiV1Model>

    fun getBinCollectionNotifications(
        request: GetBinCollectionNotificationsRequestRestApiV1Model,
        notificationHandler: Handler<NextBinCollectionRestApiV1Model>
    ): CompletableFuture<String>

    fun endGetBinCollectionNotifications(subscriptionId: String): CompletableFuture<Void>
}
