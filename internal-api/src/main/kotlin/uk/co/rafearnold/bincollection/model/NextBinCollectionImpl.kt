package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

internal data class NextBinCollectionImpl(
    override val binTypes: Set<BinType>,
    override val dateOfCollection: LocalDate
) : NextBinCollection
