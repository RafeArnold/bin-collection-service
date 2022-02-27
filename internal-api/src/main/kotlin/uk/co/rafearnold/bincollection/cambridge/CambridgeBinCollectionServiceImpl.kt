package uk.co.rafearnold.bincollection.cambridge

import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NextBinCollection
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class CambridgeBinCollectionServiceImpl @Inject constructor(
    private val apiClient: CambridgeBinCollectionApiClient,
    private val modelFactory: ModelFactory
) : CambridgeBinCollectionService {

    override fun getNextBinCollection(postcode: String, houseNumber: String): CompletableFuture<NextBinCollection> =
        apiClient.getPostcodeData(postcode = postcode)
            .thenCompose { postcodeData: GetPostcodeDataResponse ->
                val addressId: String =
                    postcodeData.addressData.firstOrNull { it.houseNumber == houseNumber }?.id
                        ?: throw NoSuchHouseNumberFoundException(postcode = postcode, houseNumber = houseNumber)
                apiClient.getBinCollectionData(addressId = addressId)
            }
            .thenApply { collectionData: GetBinCollectionDataResponse ->
                val nextCollection: BinCollectionData =
                    collectionData.collections
                        .filter { it.date > ZonedDateTime.now() }
                        .minByOrNull { it.date }
                        ?: throw NoBinCollectionDataFoundException(postcode = postcode, houseNumber = houseNumber)
                nextCollection.toNextBinCollection()
            }
            .exceptionally { throw if (it is CambridgeBinCollectionApiClientException) ApiException(cause = it) else it }

    private fun BinCollectionData.toNextBinCollection(): NextBinCollection =
        modelFactory.createNextBinCollection(
            binTypes = this.roundTypes.map { it.toBinType() }.toSet(),
            dateOfCollection = this.date.toLocalDate()
        )

    private fun RoundType.toBinType(): BinType =
        when (this) {
            RoundType.ORGANIC -> BinType.ORGANIC
            RoundType.RECYCLE -> BinType.RECYCLING
            RoundType.DOMESTIC -> BinType.GENERAL
        }
}
