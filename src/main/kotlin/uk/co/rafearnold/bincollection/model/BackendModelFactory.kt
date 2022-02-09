package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

interface BackendModelFactory : ModelFactory {

    fun createNextBinCollection(binTypes: Set<BinType>, dateOfCollection: LocalDate): NextBinCollection
}
