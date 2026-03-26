package com.quickmart.dto.admin

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

@Schema(description = "Представляет полезную нагрузку запроса на изменение остатка.")
data class InventoryUpdateRequest(
    @field:Min(0)
    @field:Schema(description = "Указывает целевое доступное количество. Минимальное значение — 0.", example = "42")
    val availableQuantity: Int,
)
