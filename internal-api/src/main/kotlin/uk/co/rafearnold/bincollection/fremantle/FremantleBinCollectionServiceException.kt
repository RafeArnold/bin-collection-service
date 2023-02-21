package uk.co.rafearnold.bincollection.fremantle

sealed class FremantleBinCollectionServiceException(
    override val message: String,
    override val cause: Throwable?
) : Throwable(message, cause)

class MultipleAddressesFoundException(addressQuery: String, addressDataList: Iterable<AddressData>) :
    FremantleBinCollectionServiceException(
        message = "Multiple addresses found for query '$addressQuery': " +
                addressDataList.joinToString(separator = "\n", prefix = "\n") { it.address },
        cause = null
    )

class NoSuchAddressFoundException(addressQuery: String) : FremantleBinCollectionServiceException(
    message = "No address found for query '$addressQuery'",
    cause = null
)

class NoBinCollectionDataFoundException(address: String) : FremantleBinCollectionServiceException(
    message = "No bin collection data found for address '$address'",
    cause = null
)

class ApiException(
    cause: Throwable?
) : FremantleBinCollectionServiceException(message = "API encountered an exception", cause = cause)
