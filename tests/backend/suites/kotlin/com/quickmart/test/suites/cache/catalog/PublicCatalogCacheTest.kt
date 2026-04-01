package com.quickmart.test.suites.cache.catalog

import com.quickmart.test.shared.cache.assertion.CacheAssertions
import com.quickmart.test.shared.cache.foundation.BaseCacheComponentTest
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Epic("Quickmart Backend Tests")
@Feature("Cache Integration")
@Owner("backend-platform")
@Tag("cache")
@DisplayName("Cache: public catalog")
class PublicCatalogCacheTest : BaseCacheComponentTest() {
    @Test
    @Story("Catalog pages")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Повторное чтение каталога использует кэш и после update товара кэш инвалидируется")
    fun shouldCacheCatalogPagesAndEvictAfterProductUpdate() {
        val fixture = catalogCacheScenario.createCatalogFixture(prefix = "catalog-page", initialStock = 11)
        val expectedPageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "name"))
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val first = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)
        val second = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)

        CacheAssertions.assertCatalogContainsProduct(first, fixture.product.name, 11)
        CacheAssertions.assertCatalogContainsProduct(second, fixture.product.name, 11)
        Mockito.verify(productRepository, Mockito.times(1)).findCatalogPage(fixture.category.id, null, expectedPageable)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)

        val renamedName = "${fixture.product.name} Updated"
        catalogCacheScenario.renameProduct(fixture, renamedName)
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val refreshed = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)

        CacheAssertions.assertCatalogContainsProduct(refreshed, renamedName, 11)
        Mockito.verify(productRepository, Mockito.times(1)).findCatalogPage(fixture.category.id, null, expectedPageable)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)
    }

    @Test
    @Story("Product card")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Повторное чтение карточки товара использует кэш, а изменение stock инвалидирует его")
    fun shouldCacheProductCardAndEvictAfterInventoryUpdate() {
        val fixture = catalogCacheScenario.createCatalogFixture(prefix = "product-card", initialStock = 5)
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val first = catalogCacheScenario.fetchProduct(fixture.product.id)
        val second = catalogCacheScenario.fetchProduct(fixture.product.id)

        CacheAssertions.assertProductCard(first, fixture.product.name, fixture.category.name, 5)
        CacheAssertions.assertProductCard(second, fixture.product.name, fixture.category.name, 5)
        Mockito.verify(productRepository, Mockito.times(1)).findById(fixture.product.id)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)

        catalogCacheScenario.updateStock(fixture.product.id, 17)
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val refreshed = catalogCacheScenario.fetchProduct(fixture.product.id)

        CacheAssertions.assertProductCard(refreshed, fixture.product.name, fixture.category.name, 17)
        Mockito.verify(productRepository, Mockito.times(1)).findById(fixture.product.id)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)
    }

    @Test
    @Story("Catalog pages")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Изменение stock инвалидирует catalog cache и обновляет availableQuantity в ответе")
    fun shouldEvictCatalogCacheAfterInventoryUpdate() {
        val fixture = catalogCacheScenario.createCatalogFixture(prefix = "catalog-stock", initialStock = 4)
        val expectedPageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "name"))
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val first = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)
        val second = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)

        CacheAssertions.assertCatalogContainsProduct(first, fixture.product.name, 4)
        CacheAssertions.assertCatalogContainsProduct(second, fixture.product.name, 4)
        Mockito.verify(productRepository, Mockito.times(1)).findCatalogPage(fixture.category.id, null, expectedPageable)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)

        catalogCacheScenario.updateStock(fixture.product.id, 15)
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val refreshed = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)

        CacheAssertions.assertCatalogContainsProduct(refreshed, fixture.product.name, 15)
        Mockito.verify(productRepository, Mockito.times(1)).findCatalogPage(fixture.category.id, null, expectedPageable)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)
    }

    @Test
    @Story("Cross-entity invalidation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Изменение категории инвалидирует product cache и обновляет categoryName в карточке товара")
    fun shouldEvictProductCacheAfterCategoryUpdate() {
        val fixture = catalogCacheScenario.createCatalogFixture(prefix = "category-rename", initialStock = 9)
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        catalogCacheScenario.fetchProduct(fixture.product.id)
        catalogCacheScenario.fetchProduct(fixture.product.id)
        Mockito.verify(productRepository, Mockito.times(1)).findById(fixture.product.id)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)

        val renamedCategory = "${fixture.category.name} Updated"
        catalogCacheScenario.renameCategory(fixture, renamedCategory)
        Mockito.clearInvocations(productRepository, inventoryStockRepository)

        val refreshed = catalogCacheScenario.fetchProduct(fixture.product.id)

        CacheAssertions.assertProductCard(refreshed, fixture.product.name, renamedCategory, 9)
        Mockito.verify(productRepository, Mockito.times(1)).findById(fixture.product.id)
        Mockito.verify(inventoryStockRepository, Mockito.times(1)).findByProductId(fixture.product.id)
    }
}
