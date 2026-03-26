package com.quickmart.dto.order

import com.quickmart.domain.enums.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "Представляет полезную нагрузку запроса на административное изменение статуса заказа.")
data class UpdateOrderStatusRequest(
    @field:NotNull
    @field:Schema(description = "Указывает целевой статус заказа.", example = "CONFIRMED")
    val status: OrderStatus,
)
