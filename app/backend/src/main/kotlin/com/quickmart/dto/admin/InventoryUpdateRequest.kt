package com.quickmart.dto.admin

import jakarta.validation.constraints.Min

data class InventoryUpdateRequest(
    @field:Min(0)
    val availableQuantity: Int,
)
