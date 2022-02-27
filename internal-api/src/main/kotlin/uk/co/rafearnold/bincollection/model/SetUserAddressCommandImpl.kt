package uk.co.rafearnold.bincollection.model

data class SetUserAddressCommandImpl(
    override val houseNumber: String,
    override val postcode: String
) : SetUserAddressCommand
