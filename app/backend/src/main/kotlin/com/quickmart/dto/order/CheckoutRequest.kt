package com.quickmart.dto.order

import com.quickmart.domain.enums.PaymentMethod
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CheckoutRequest(
    @field:NotNull
    val addressId: UUID,
    @field:NotNull
    val deliverySlotId: UUID,
    val promoCode: String?,
    @field:NotNull
    val paymentMethod: PaymentMethod,
)
