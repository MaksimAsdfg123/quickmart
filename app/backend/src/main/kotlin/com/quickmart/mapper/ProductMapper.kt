package com.quickmart.mapper

import com.quickmart.domain.entity.Product
import com.quickmart.dto.product.ProductResponse
import com.quickmart.repository.InventoryStockRepository
import org.springframework.stereotype.Component

@Component
class ProductMapper(
    private val inventoryStockRepository: InventoryStockRepository,
) {
    fun toResponse(product: Product): ProductResponse =
        ProductResponse(
            id = product.id!!,
            name = product.name,
            description = product.description,
            price = product.price,
            categoryId = product.category.id!!,
            categoryName = product.category.name,
            imageUrl = product.imageUrl,
            active = product.active,
            availableQuantity = inventoryStockRepository.findByProductId(product.id!!)?.availableQuantity ?: 0,
        )
}
