package com.quickmart.test.shared.cache.data

import com.quickmart.dto.category.CategoryRequest
import com.quickmart.dto.product.ProductRequest
import com.quickmart.test.shared.common.util.RandomDataUtils
import java.math.BigDecimal
import java.util.UUID

object CacheTestDataFactory {
    fun categoryRequest(
        prefix: String,
        active: Boolean = true,
    ): CategoryRequest =
        CategoryRequest(
            name = RandomDataUtils.uniqueName("Cache Category $prefix"),
            description = "Cache category for $prefix",
            active = active,
        )

    fun productRequest(
        categoryId: UUID,
        prefix: String,
        active: Boolean = true,
        price: BigDecimal = BigDecimal("199.00"),
    ): ProductRequest =
        ProductRequest(
            name = RandomDataUtils.uniqueName("Cache Product $prefix"),
            description = "Cache product for $prefix",
            price = price,
            categoryId = categoryId,
            imageUrl = "https://cdn.quickmart.local/$prefix.png",
            active = active,
        )
}
