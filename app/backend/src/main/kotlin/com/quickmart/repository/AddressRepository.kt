package com.quickmart.repository

import com.quickmart.domain.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AddressRepository : JpaRepository<Address, UUID> {
    fun findAllByUserId(userId: UUID): List<Address>

    fun findByIdAndUserId(
        id: UUID,
        userId: UUID,
    ): Address?
}
