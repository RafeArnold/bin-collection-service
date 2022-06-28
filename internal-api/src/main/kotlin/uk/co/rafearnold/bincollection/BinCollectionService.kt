package uk.co.rafearnold.bincollection

import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import java.util.concurrent.CompletableFuture

interface BinCollectionService {

    fun subscribeToNextBinCollectionNotifications(
        houseNumber: String,
        postcode: String,
        notificationTimes: Set<NotificationTimeSetting>,
        notificationHandler: Handler<NextBinCollection>
    ): CompletableFuture<String>

    fun unsubscribeFromNextBinCollectionNotifications(subscriptionId: String): CompletableFuture<Void>

    fun getNextBinCollection(houseNumber: String, postcode: String): CompletableFuture<NextBinCollection>
}
