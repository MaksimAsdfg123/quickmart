package com.quickmart.mapper

import com.quickmart.domain.entity.InventoryStock
import com.quickmart.dto.admin.InventoryStockResponse
import org.springframework.stereotype.Component

@Component
class InventoryMapper {
    fun toResponse(stock: InventoryStock): InventoryStockResponse =
        InventoryStockResponse(
            id = stock.id!!,
            productId = stock.product.id!!,
            productName = stock.product.name,
            categoryName = stock.product.category.name,
            price = stock.product.price,
            productActive = stock.product.active,
            availableQuantity = stock.availableQuantity,
        )
}
