package com.quickmart.dto.cart

import jakarta.validation.constraints.Min

data class UpdateCartItemRequest(
    @field:Min(0)
    val quantity: Int,
)
