package uk.co.rafearnold.bincollection.model

internal data class SetUserAddressCommandImpl(
    override val addressInfo: AddressInfo,
) : SetUserAddressCommand
