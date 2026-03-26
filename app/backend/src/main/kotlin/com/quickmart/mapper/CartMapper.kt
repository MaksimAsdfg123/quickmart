package com.quickmart.mapper

import com.quickmart.domain.entity.Cart
import com.quickmart.dto.cart.CartItemResponse
import com.quickmart.dto.cart.CartResponse
import com.quickmart.repository.InventoryStockRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CartMapper(
    private val inventoryStockRepository: InventoryStockRepository,
) {
    fun toResponse(cart: Cart): CartResponse {
        val items =
            cart.items.map { item ->
                val unitPrice = item.product.price
                val lineTotal = unitPrice.multiply(item.quantity.toBigDecimal())
                CartItemResponse(
                    id = item.id!!,
                    productId = item.product.id!!,
                    productName = item.product.name,
                    unitPrice = unitPrice,
                    quantity = item.quantity,
                    lineTotal = lineTotal,
                    availableQuantity = inventoryStockRepository.findByProductId(item.product.id!!)?.availableQuantity ?: 0,
                )
            }

        return CartResponse(
            id = cart.id!!,
            items = items,
            totalItems = items.sumOf { it.quantity },
            subtotal = items.fold(BigDecimal.ZERO) { acc, item -> acc + item.lineTotal },
        )
    }
}
