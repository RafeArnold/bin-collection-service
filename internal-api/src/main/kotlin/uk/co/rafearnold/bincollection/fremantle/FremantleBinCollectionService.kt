package uk.co.rafearnold.bincollection.fremantle

import uk.co.rafearnold.bincollection.model.NextBinCollection
import java.util.concurrent.CompletableFuture

interface FremantleBinCollectionService {

    fun getNextBinCollection(addressQuery: String): CompletableFuture<NextBinCollection>
}
