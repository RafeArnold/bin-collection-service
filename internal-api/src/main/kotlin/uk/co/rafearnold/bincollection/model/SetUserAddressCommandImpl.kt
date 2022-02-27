package uk.co.rafearnold.bincollection.model

internal data class SetUserAddressCommandImpl(
    override val houseNumber: String,
    override val postcode: String
) : SetUserAddressCommand
