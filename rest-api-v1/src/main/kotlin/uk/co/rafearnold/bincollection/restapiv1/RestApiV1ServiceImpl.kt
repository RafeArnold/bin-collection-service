package uk.co.rafearnold.bincollection.restapiv1

import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.Handler
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class RestApiV1ServiceImpl @Inject constructor(
    private val binCollectionService: BinCollectionService,
    private val modelMapper: RestApiV1ModelMapper
) : RestApiV1Service {

    override fun getNextBinCollection(request: GetNextBinCollectionRequestRestApiV1Model): CompletableFuture<GetNextBinCollectionResponseRestApiV1Model> =
        CompletableFuture.completedFuture(null).thenCompose {
            binCollectionService.getNextBinCollection(addressInfo = modelMapper.mapToAddressInfo(addressInfo = request.addressInfo))
        }.thenApply {
            GetNextBinCollectionResponseRestApiV1Model(
                nextBinCollection = modelMapper.mapToNextBinCollectionRestApiV1Model(nextBinCollection = it)
            )
        }

    override fun getBinCollectionNotifications(
        request: GetBinCollectionNotificationsRequestRestApiV1Model,
        notificationHandler: Handler<NextBinCollectionRestApiV1Model>
    ): CompletableFuture<String> =
        CompletableFuture.completedFuture(null).thenCompose {
            binCollectionService.subscribeToNextBinCollectionNotifications(
                addressInfo = modelMapper.mapToAddressInfo(addressInfo = request.addressInfo),
                notificationTimes = request.notificationTimes.map { modelMapper.mapToNotificationTimeSetting(it) }
                    .toSet()
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
