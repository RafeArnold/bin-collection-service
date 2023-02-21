package uk.co.rafearnold.bincollection.discordbot.repository.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "region")
@JsonSubTypes(
    JsonSubTypes.Type(value = StoredCambridgeAddressInfo::class, name = "CAMBRIDGE"),
    JsonSubTypes.Type(value = StoredFremantleAddressInfo::class, name = "FREMANTLE"),
)
sealed interface StoredAddressInfo

data class StoredCambridgeAddressInfo(
    val houseNumber: String,
    val postcode: String,
) : StoredAddressInfo

data class StoredFremantleAddressInfo(
    val addressQuery: String,
) : StoredAddressInfo
