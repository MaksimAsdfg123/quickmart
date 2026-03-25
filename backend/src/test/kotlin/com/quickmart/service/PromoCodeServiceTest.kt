package com.quickmart.service

import com.quickmart.domain.entity.PromoCode
import com.quickmart.domain.enums.PromoType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.math.BigDecimal

class PromoCodeServiceTest {
    private val service =
        PromoCodeService(
            Mockito.mock(com.quickmart.repository.PromoCodeRepository::class.java),
            Mockito.mock(com.quickmart.mapper.PromoCodeMapper::class.java),
        )

    @Test
    fun `fixed discount should not exceed subtotal`() {
        val promo =
            PromoCode().apply {
                type = PromoType.FIXED
                value = BigDecimal("500.00")
            }

        val discount = service.calculateDiscount(BigDecimal("300.00"), promo)
        assertEquals(BigDecimal("300.00"), discount)
    }

    @Test
    fun `percent discount should be calculated correctly`() {
        val promo =
            PromoCode().apply {
                type = PromoType.PERCENT
                value = BigDecimal("10")
            }

        val discount = service.calculateDiscount(BigDecimal("2500.00"), promo)
        assertEquals(BigDecimal("250.00"), discount)
    }

    @Test
    fun `percent discount should not exceed subtotal`() {
        val promo =
            PromoCode().apply {
                type = PromoType.PERCENT
                value = BigDecimal("200")
            }

        val discount = service.calculateDiscount(BigDecimal("300.00"), promo)
        assertEquals(BigDecimal("300.00"), discount)
    }
}
