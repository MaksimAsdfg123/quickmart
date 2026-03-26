package com.quickmart.dto.order

import com.quickmart.domain.enums.OrderStatus
import jakarta.validation.constraints.NotNull

data class UpdateOrderStatusRequest(
    @field:NotNull
    val status: OrderStatus,
)
