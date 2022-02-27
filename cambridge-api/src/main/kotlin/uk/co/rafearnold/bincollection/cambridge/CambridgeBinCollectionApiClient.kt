package uk.co.rafearnold.bincollection.cambridge

import java.util.concurrent.CompletableFuture

interface CambridgeBinCollectionApiClient {

    fun getPostcodeData(postcode: String): CompletableFuture<GetPostcodeDataResponse>

    fun getBinCollectionData(addressId: String): CompletableFuture<GetBinCollectionDataResponse>
}
