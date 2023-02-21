package uk.co.rafearnold.bincollection.fremantle

import uk.co.rafearnold.bincollection.model.BinType
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.NextBinCollection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class FremantleBinCollectionServiceImpl @Inject constructor(
    private val apiClient: FremantleBinCollectionApiClient,
    private val modelFactory: ModelFactory,
) : FremantleBinCollectionService {

    override fun getNextBinCollection(addressQuery: String): CompletableFuture<NextBinCollection> =
        apiClient.getAddressData(query = addressQuery)
            .thenCompose { (addressDataList): GetAddressDataResponse ->
                if (addressDataList.size > 1) {
                    throw MultipleAddressesFoundException(
                        addressQuery = addressQuery,
                        addressDataList = addressDataList,
                    )
                }
                val addressData: AddressData =
                    addressDataList.firstOrNull() ?: throw NoSuchAddressFoundException(addressQuery = addressQuery)
                apiClient.getBinCollectionData(addressData = addressData)
                    .thenApply {
                        it.toNextBinCollection()
                            ?: throw NoBinCollectionDataFoundException(address = addressData.address)
                    }
            }
            .exceptionally { throw if (it is FremantleBinCollectionApiClientException) ApiException(cause = it) else it }

    private fun GetBinCollectionDataResponse.toNextBinCollection(): NextBinCollection? =
        dataList
            .mapNotNull { it.toNextBinCollection() }
            .groupBy { it.dateOfCollection }
            .mapValues {
                modelFactory.createNextBinCollection(
                    it.value.flatMap { binCollection -> binCollection.binTypes }.toSet(),
                    it.key,
                )
            }
            .values
            .minByOrNull { it.dateOfCollection }

    private fun BinCollectionData.toNextBinCollection(): NextBinCollection? =
        runCatching {
            modelFactory.createNextBinCollection(
                dateOfCollection = LocalDate.parse(this.details, DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")),
                binTypes = setOf(this.name.toBinType() ?: return null),
            )
        }.getOrNull()

    private fun String.toBinType(): BinType? =
        when {
            this.contains(other = "fogo", ignoreCase = true) -> BinType.ORGANIC
            this.contains(other = "recycling", ignoreCase = true) -> BinType.RECYCLING
            this.contains(other = "general waste", ignoreCase = true) -> BinType.GENERAL
            else -> null
        }
}
