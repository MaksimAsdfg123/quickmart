package com.quickmart.repository

import com.quickmart.domain.entity.PromoCode
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PromoCodeRepository : JpaRepository<PromoCode, UUID> {
    fun findByCodeIgnoreCase(code: String): Optional<PromoCode>
}
