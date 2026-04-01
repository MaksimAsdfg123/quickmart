package com.quickmart.test.shared.cache.scenario

import com.quickmart.dto.PageResponse
import com.quickmart.dto.category.CategoryResponse
import com.quickmart.dto.product.ProductResponse
import com.quickmart.service.CategoryService
import com.quickmart.service.InventoryService
import com.quickmart.service.ProductService
import com.quickmart.test.shared.cache.data.CacheTestDataFactory
import com.quickmart.test.shared.cache.data.CatalogFixture
import com.quickmart.test.shared.foundation.allureStep
import java.util.UUID

class CatalogCacheScenario(
    private val productService: ProductService,
    private val categoryService: CategoryService,
    private val inventoryService: InventoryService,
) {
    fun createCatalogFixture(
        prefix: String,
        initialStock: Int = 7,
    ): CatalogFixture =
        allureStep("Подготовить fixture каталога для кэширования: $prefix") {
            val category = categoryService.create(CacheTestDataFactory.categoryRequest(prefix))
            val product =
                productService.create(
                    CacheTestDataFactory.productRequest(
                        categoryId = category.id,
                        prefix = prefix,
                    ),
                )
            inventoryService.updateStock(product.id, initialStock)

            CatalogFixture(
                category = category,
                product = product,
            )
        }

    fun fetchCatalog(
        categoryId: UUID? = null,
        query: String? = null,
        page: Int = 0,
        size: Int = 12,
    ): PageResponse<ProductResponse> =
        allureStep("Прочитать публичный каталог товаров") {
            productService.getCatalog(categoryId, query, page, size)
        }

    fun fetchProduct(productId: UUID): ProductResponse =
        allureStep("Прочитать карточку товара $productId") {
            productService.getProduct(productId)
        }

    fun fetchPublicCategories(): List<CategoryResponse> =
        allureStep("Прочитать публичный список категорий") {
            categoryService.getPublicCategories()
        }

    fun renameProduct(
        fixture: CatalogFixture,
        newName: String,
    ): ProductResponse =
        allureStep("Переименовать товар ${fixture.product.id}") {
            productService.update(
                fixture.product.id,
                CacheTestDataFactory.productRequest(
                    categoryId = fixture.category.id,
                    prefix = newName,
                    active = fixture.product.active,
                    price = fixture.product.price,
                ).copy(
                    name = newName,
                    description = fixture.product.description,
                    imageUrl = fixture.product.imageUrl,
                ),
            )
        }

    fun renameCategory(
        fixture: CatalogFixture,
        newName: String,
    ): CategoryResponse =
        allureStep("Переименовать категорию ${fixture.category.id}") {
            categoryService.update(
                fixture.category.id,
                CacheTestDataFactory.categoryRequest(newName, active = fixture.category.active).copy(
                    name = newName,
                    description = fixture.category.description,
                ),
            )
        }

    fun deactivateCategory(fixture: CatalogFixture): CategoryResponse =
        allureStep("Скрыть категорию ${fixture.category.id} из публичного списка") {
            categoryService.setActive(fixture.category.id, false)
        }

    fun updateStock(
        productId: UUID,
        newQuantity: Int,
    ) =
        allureStep("Обновить складской остаток товара $productId до $newQuantity") {
            inventoryService.updateStock(productId, newQuantity)
        }
}
