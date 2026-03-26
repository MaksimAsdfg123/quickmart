package com.quickmart.domain.entity

import com.quickmart.domain.enums.PromoType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "promo_codes")
class PromoCode : BaseEntity() {
    @Column(name = "code", nullable = false, unique = true)
    lateinit var code: String

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: PromoType = PromoType.FIXED

    @Column(name = "value", nullable = false, precision = 12, scale = 2)
    var value: BigDecimal = BigDecimal.ZERO

    @Column(name = "min_order_amount", nullable = false, precision = 12, scale = 2)
    var minOrderAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @Column(name = "valid_from")
    var validFrom: LocalDateTime? = null

    @Column(name = "valid_to")
    var validTo: LocalDateTime? = null

    @Column(name = "usage_limit")
    var usageLimit: Int? = null

    @Column(name = "used_count", nullable = false)
    var usedCount: Int = 0
}
