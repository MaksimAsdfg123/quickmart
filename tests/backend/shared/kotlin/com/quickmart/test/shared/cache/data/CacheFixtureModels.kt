package com.quickmart.test.shared.cache.data

import com.quickmart.dto.category.CategoryResponse
import com.quickmart.dto.product.ProductResponse

data class CatalogFixture(
    val category: CategoryResponse,
    val product: ProductResponse,
)
