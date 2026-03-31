package com.quickmart.events

import java.util.UUID

data class OrderEventItemPayload(
    val productId: UUID,
    val productName: String,
    val quantity: Int,
)
