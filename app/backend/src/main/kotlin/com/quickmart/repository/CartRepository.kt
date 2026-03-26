package com.quickmart.repository

import com.quickmart.domain.entity.Cart
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface CartRepository : JpaRepository<Cart, UUID> {
    @EntityGraph(attributePaths = ["items", "items.product", "items.product.category"])
    fun findByUserIdAndActiveTrue(userId: UUID): Optional<Cart>
}
