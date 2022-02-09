package uk.co.rafearnold.bincollection.restapiv1

import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.Handler
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class RestApiV1ServiceImpl @Inject constructor(
    private val binCollectionService: BinCollectionService,
    private val modelMapper: RestApiV1ModelMapper
) : RestApiV1Service {

    override fun getBinCollectionNotifications(
        houseNumber: String,
        postcode: String,
        notificationTimes: Set<NotificationTimeSettingRestApiV1Model>,
        notificationHandler: Handler<NextBinCollectionRestApiV1Model>
    ): CompletableFuture<String> =
        CompletableFuture.completedFuture(null).thenCompose {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                houseNumber = houseNumber,
                postcode = postcode,
                notificationTimes = notificationTimes.map { modelMapper.mapToNotificationTimeSetting(it) }.toSet()
            ) {
                val nextBinCollectionApiModel: NextBinCollectionRestApiV1Model =
                    modelMapper.mapToNextBinCollectionRestApiV1Model(nextBinCollection = it)
                CompletableFuture.completedFuture(null).thenCompose {
                    notificationHandler.handle(nextBinCollectionApiModel)
                }
            }
        }

    override fun endGetBinCollectionNotifications(subscriptionId: String): CompletableFuture<Void> =
        binCollectionService.unsubscribeFromNextBinCollectionNotifications(subscriptionId = subscriptionId)
}
