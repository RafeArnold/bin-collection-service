package uk.co.rafearnold.bincollection.restapiv1

import java.time.LocalDate

data class NextBinCollectionRestApiV1Model(
    val binTypes: Set<BinTypeRestApiV1Model>,
    val dateOfCollection: LocalDate
)
