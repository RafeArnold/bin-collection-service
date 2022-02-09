package uk.co.rafearnold.bincollection.model

import java.time.LocalDate

interface NextBinCollection {
    val binTypes: Set<BinType>
    val dateOfCollection: LocalDate
}
