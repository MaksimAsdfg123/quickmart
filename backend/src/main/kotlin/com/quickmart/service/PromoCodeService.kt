package com.quickmart.service

import com.quickmart.domain.entity.PromoCode
import com.quickmart.domain.enums.PromoType
import com.quickmart.dto.promo.PromoCodeRequest
import com.quickmart.dto.promo.PromoCodeResponse
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.PromoCodeMapper
import com.quickmart.repository.PromoCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID

@Service
class PromoCodeService(
    private val promoCodeRepository: PromoCodeRepository,
    private val promoCodeMapper: PromoCodeMapper,
) {
    fun getAll(): List<PromoCodeResponse> =
        promoCodeRepository
            .findAll()
            .sortedBy { it.code }
            .map(promoCodeMapper::toResponse)

    fun resolveAndValidate(
        code: String?,
        subtotal: BigDecimal,
    ): PromoCode? {
        if (code.isNullOrBlank()) {
            return null
        }

        val promo =
            promoCodeRepository
                .findByCodeIgnoreCase(code.trim())
                .orElseThrow { BusinessException("Промокод не найден") }

        if (!promo.active) {
            throw BusinessException("Промокод неактивен")
        }

        val now = LocalDateTime.now()
        if (promo.validFrom != null && now.isBefore(promo.validFrom)) {
            throw BusinessException("Промокод еще не начал действовать")
        }
        if (promo.validTo != null && now.isAfter(promo.validTo)) {
            throw BusinessException("Срок действия промокода истек")
        }

        if (promo.usageLimit != null && promo.usedCount >= promo.usageLimit!!) {
            throw BusinessException("Лимит использования промокода исчерпан")
        }

        if (subtotal < promo.minOrderAmount) {
            throw BusinessException("Сумма заказа меньше минимальной для промокода")
        }

        return promo
    }

    fun calculateDiscount(
        subtotal: BigDecimal,
        promoCode: PromoCode?,
    ): BigDecimal {
        if (promoCode == null) {
            return BigDecimal.ZERO
        }

        return when (promoCode.type) {
            PromoType.FIXED -> promoCode.value.min(subtotal)
            PromoType.PERCENT -> {
                val raw =
                    subtotal
                        .multiply(promoCode.value)
                        .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
                raw.min(subtotal)
            }
        }
    }

    @Transactional
    fun incrementUsage(promoCode: PromoCode) {
        promoCode.usedCount += 1
        promoCodeRepository.save(promoCode)
    }

    @Transactional
    fun create(request: PromoCodeRequest): PromoCodeResponse {
        val promo =
            PromoCode().apply {
                code = request.code.trim().uppercase()
                type = request.type
                value = request.value
                minOrderAmount = request.minOrderAmount
                active = request.active
                validFrom = request.validFrom
                validTo = request.validTo
                usageLimit = request.usageLimit
                usedCount = 0
            }
        validatePromo(promo)
        return promoCodeMapper.toResponse(promoCodeRepository.save(promo))
    }

    @Transactional
    fun update(
        id: UUID,
        request: PromoCodeRequest,
    ): PromoCodeResponse {
        val promo = promoCodeRepository.findById(id).orElseThrow { NotFoundException("Промокод не найден") }
        promo.code = request.code.trim().uppercase()
        promo.type = request.type
        promo.value = request.value
        promo.minOrderAmount = request.minOrderAmount
        promo.active = request.active
        promo.validFrom = request.validFrom
        promo.validTo = request.validTo
        promo.usageLimit = request.usageLimit
        validatePromo(promo)
        return promoCodeMapper.toResponse(promoCodeRepository.save(promo))
    }

    @Transactional
    fun toggle(
        id: UUID,
        active: Boolean,
    ): PromoCodeResponse {
        val promo = promoCodeRepository.findById(id).orElseThrow { NotFoundException("Промокод не найден") }
        promo.active = active
        return promoCodeMapper.toResponse(promoCodeRepository.save(promo))
    }

    private fun validatePromo(promo: PromoCode) {
        if (promo.type == PromoType.PERCENT && (promo.value <= BigDecimal.ZERO || promo.value > BigDecimal("100"))) {
            throw BusinessException("Процент промокода должен быть в диапазоне (0, 100]")
        }
        if (promo.type == PromoType.FIXED && promo.value <= BigDecimal.ZERO) {
            throw BusinessException("Фиксированная скидка должна быть больше 0")
        }
        if (promo.validFrom != null && promo.validTo != null && promo.validFrom!!.isAfter(promo.validTo)) {
            throw BusinessException("Дата начала действия не может быть позже даты окончания")
        }
        if (promo.usageLimit != null && promo.usageLimit!! <= 0) {
            throw BusinessException("Лимит использования промокода должен быть больше 0")
        }
    }
}
