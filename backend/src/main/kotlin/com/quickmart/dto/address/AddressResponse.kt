package com.quickmart.dto.address

import java.util.UUID

data class AddressResponse(
    val id: UUID,
    val label: String,
    val city: String,
    val street: String,
    val house: String,
    val apartment: String?,
    val entrance: String?,
    val floor: String?,
    val comment: String?,
    val isDefault: Boolean,
)
