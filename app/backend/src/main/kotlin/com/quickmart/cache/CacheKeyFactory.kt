package com.quickmart.cache

import org.springframework.stereotype.Component
import java.util.UUID

@Component("cacheKeyFactory")
class CacheKeyFactory {
    fun publicCatalog(
        categoryId: UUID?,
        query: String?,
        page: Int,
        size: Int,
    ): String {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 200)
        val normalizedCategory = categoryId?.toString() ?: "all"
        val normalizedQuery =
            query
                ?.trim()
                ?.lowercase()
                ?.replace(Regex("\\s+"), " ")
                ?.takeIf { it.isNotBlank() }
                ?: "_"

        return "catalog:category=$normalizedCategory:query=$normalizedQuery:page=$normalizedPage:size=$normalizedSize"
    }

    fun publicProduct(id: UUID): String = "product:$id"

    fun singleton(scope: String): String = "scope:$scope"
}
