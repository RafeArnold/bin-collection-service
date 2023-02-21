package uk.co.rafearnold.bincollection.fremantle

import java.util.concurrent.CompletableFuture

interface FremantleBinCollectionApiClient {

    fun getAddressData(query: String): CompletableFuture<GetAddressDataResponse>

    fun getBinCollectionData(addressData: AddressData): CompletableFuture<GetBinCollectionDataResponse>
}
