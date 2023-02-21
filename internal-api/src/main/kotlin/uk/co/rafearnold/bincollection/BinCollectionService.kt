package uk.co.rafearnold.bincollection

import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import uk.co.rafearnold.bincollection.model.AddressInfo
import java.util.concurrent.CompletableFuture

interface BinCollectionService {

    fun subscribeToNextBinCollectionNotifications(
        addressInfo: AddressInfo,
        notificationTimes: Set<NotificationTimeSetting>,
        notificationHandler: Handler<NextBinCollection>,
    ): CompletableFuture<String>

    fun unsubscribeFromNextBinCollectionNotifications(subscriptionId: String): CompletableFuture<Void>

    fun getNextBinCollection(addressInfo: AddressInfo): CompletableFuture<NextBinCollection>
}
