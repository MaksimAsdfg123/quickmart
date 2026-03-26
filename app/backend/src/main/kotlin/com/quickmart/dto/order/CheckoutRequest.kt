package com.quickmart.dto.order

import com.quickmart.domain.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "Представляет полезную нагрузку запроса на оформление заказа.")
data class CheckoutRequest(
    @field:NotNull
    @field:Schema(description = "Идентифицирует адрес доставки, принадлежащий аутентифицированному пользователю. Формат: UUID.", example = "20000000-0000-0000-0000-000000000001")
    val addressId: UUID,
    @field:NotNull
    @field:Schema(description = "Идентифицирует слот доставки. Формат: UUID.", example = "70000000-0000-0000-0000-000000000001")
    val deliverySlotId: UUID,
    @field:Schema(description = "Указывает промокод, подлежащий применению. Не передавайте поле, если промокод не используется.", example = "SAVE10")
    val promoCode: String?,
    @field:NotNull
    @field:Schema(description = "Указывает запрошенный способ оплаты.", example = "CARD")
    val paymentMethod: PaymentMethod,
)

