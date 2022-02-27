package uk.co.rafearnold.bincollection.cambridge

import java.time.ZonedDateTime

data class BinCollectionData(
    val date: ZonedDateTime,
    val roundTypes: Set<RoundType>
)
