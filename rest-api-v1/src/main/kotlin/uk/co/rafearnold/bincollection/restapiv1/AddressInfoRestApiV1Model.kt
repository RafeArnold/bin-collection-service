package uk.co.rafearnold.bincollection.restapiv1

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "region")
@JsonSubTypes(
    JsonSubTypes.Type(value = CambridgeAddressInfoRestApiV1Model::class, name = "CAMBRIDGE"),
    JsonSubTypes.Type(value = FremantleAddressInfoRestApiV1Model::class, name = "FREMANTLE"),
)
sealed interface AddressInfoRestApiV1Model

data class CambridgeAddressInfoRestApiV1Model(
    val houseNumber: String,
    val postcode: String,
) : AddressInfoRestApiV1Model

data class FremantleAddressInfoRestApiV1Model(
    val addressQuery: String,
) : AddressInfoRestApiV1Model
