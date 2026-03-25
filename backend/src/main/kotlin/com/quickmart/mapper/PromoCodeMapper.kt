package com.quickmart.mapper

import com.quickmart.domain.entity.PromoCode
import com.quickmart.dto.promo.PromoCodeResponse
import org.springframework.stereotype.Component

@Component
class PromoCodeMapper {
    fun toResponse(promoCode: PromoCode): PromoCodeResponse =
        PromoCodeResponse(
            id = promoCode.id!!,
            code = promoCode.code,
            type = promoCode.type,
            value = promoCode.value,
            minOrderAmount = promoCode.minOrderAmount,
            active = promoCode.active,
            validFrom = promoCode.validFrom,
            validTo = promoCode.validTo,
            usageLimit = promoCode.usageLimit,
            usedCount = promoCode.usedCount,
        )
}
