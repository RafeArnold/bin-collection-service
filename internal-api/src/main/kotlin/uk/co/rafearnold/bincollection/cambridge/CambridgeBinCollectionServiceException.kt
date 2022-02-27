package uk.co.rafearnold.bincollection.cambridge

sealed class CambridgeBinCollectionServiceException(
    override val message: String,
    override val cause: Throwable?
) : Throwable(message, cause)

class NoSuchHouseNumberFoundException(
    postcode: String,
    houseNumber: String
) : CambridgeBinCollectionServiceException(
    message = "House number '$houseNumber' not found in postcode '$postcode'",
    cause = null
)

class NoBinCollectionDataFoundException(
    postcode: String,
    houseNumber: String
) : CambridgeBinCollectionServiceException(
    message = "No bin collection data found for house number '$houseNumber', postcode '$postcode'",
    cause = null
)

class ApiException(
    cause: Throwable?
) : CambridgeBinCollectionServiceException(message = "API encountered an exception", cause = cause)
