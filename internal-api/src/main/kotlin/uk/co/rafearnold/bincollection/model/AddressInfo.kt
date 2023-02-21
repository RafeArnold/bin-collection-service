package uk.co.rafearnold.bincollection.model

sealed interface AddressInfo

interface CambridgeAddressInfo : AddressInfo {
    val houseNumber: String
    val postcode: String
}

interface FremantleAddressInfo : AddressInfo {
    val addressQuery: String
}

internal data class CambridgeAddressInfoImpl(
    override val houseNumber: String,
    override val postcode: String,
) : CambridgeAddressInfo

internal data class FremantleAddressInfoImpl(
    override val addressQuery: String,
) : FremantleAddressInfo
