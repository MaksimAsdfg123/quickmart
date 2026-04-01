package com.quickmart.test.shared.cache.assertion

import com.quickmart.dto.PageResponse
import com.quickmart.dto.category.CategoryResponse
import com.quickmart.dto.product.ProductResponse
import com.quickmart.test.shared.foundation.allureStep
import org.assertj.core.api.Assertions.assertThat

object CacheAssertions {
    fun assertCatalogContainsProduct(
        response: PageResponse<ProductResponse>,
        expectedName: String,
        expectedQuantity: Int,
    ) {
        allureStep("Проверить данные товара в ответе каталога") {
            assertThat(response.content).isNotEmpty
            assertThat(response.content.map { it.name }).contains(expectedName)
            assertThat(response.content.first { it.name == expectedName }.availableQuantity).isEqualTo(expectedQuantity)
        }
    }

    fun assertProductCard(
        response: ProductResponse,
        expectedName: String,
        expectedCategoryName: String,
        expectedQuantity: Int,
    ) {
        allureStep("Проверить данные карточки товара") {
            assertThat(response.name).isEqualTo(expectedName)
            assertThat(response.categoryName).isEqualTo(expectedCategoryName)
            assertThat(response.availableQuantity).isEqualTo(expectedQuantity)
        }
    }

    fun assertPublicCategories(
        categories: List<CategoryResponse>,
        expectedNames: Set<String>,
    ) {
        allureStep("Проверить состав публичного списка категорий") {
            assertThat(categories.map { it.name }.toSet()).isEqualTo(expectedNames)
        }
    }
}
