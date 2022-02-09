package uk.co.rafearnold.bincollection.cambridge

import uk.co.rafearnold.bincollection.model.NextBinCollection
import java.util.concurrent.CompletableFuture

interface CambridgeBinCollectionService {

    fun getNextBinCollection(postcode: String, houseNumber: String): CompletableFuture<NextBinCollection>
}
