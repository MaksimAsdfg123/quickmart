package com.quickmart.repository

import com.quickmart.domain.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CartItemRepository : JpaRepository<CartItem, UUID> {
    fun findByCartIdAndProductId(
        cartId: UUID,
        productId: UUID,
    ): CartItem?
}
