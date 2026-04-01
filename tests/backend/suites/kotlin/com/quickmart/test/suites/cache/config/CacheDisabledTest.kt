package com.quickmart.test.suites.cache.config

import com.quickmart.test.shared.cache.assertion.CacheAssertions
import com.quickmart.test.shared.cache.foundation.BaseCacheDisabledComponentTest
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
@DisplayName("Cache: disabled mode")
class CacheDisabledTest : BaseCacheDisabledComponentTest() {
    @Test
    @Story("Operational fallback")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("При APP_CACHE_ENABLED=false бизнес-логика работает без кэша и повторные чтения идут в репозитории")
    fun shouldFallbackToDirectReadsWhenCacheIsDisabled() {
        val fixture = catalogCacheScenario.createCatalogFixture(prefix = "cache-disabled", initialStock = 13)
        val expectedPageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "name"))
        Mockito.clearInvocations(productRepository, categoryRepository, inventoryStockRepository)

        val firstCatalog = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)
        val secondCatalog = catalogCacheScenario.fetchCatalog(categoryId = fixture.category.id)
        val firstProduct = catalogCacheScenario.fetchProduct(fixture.product.id)
        val secondProduct = catalogCacheScenario.fetchProduct(fixture.product.id)
        val firstCategories = catalogCacheScenario.fetchPublicCategories()
        val secondCategories = catalogCacheScenario.fetchPublicCategories()

        CacheAssertions.assertCatalogContainsProduct(firstCatalog, fixture.product.name, 13)
        CacheAssertions.assertCatalogContainsProduct(secondCatalog, fixture.product.name, 13)
        CacheAssertions.assertProductCard(firstProduct, fixture.product.name, fixture.category.name, 13)
        CacheAssertions.assertProductCard(secondProduct, fixture.product.name, fixture.category.name, 13)
        CacheAssertions.assertPublicCategories(firstCategories, setOf(fixture.category.name))
        CacheAssertions.assertPublicCategories(secondCategories, setOf(fixture.category.name))

        Mockito.verify(productRepository, Mockito.times(2)).findCatalogPage(fixture.category.id, null, expectedPageable)
        Mockito.verify(productRepository, Mockito.times(2)).findById(fixture.product.id)
        Mockito.verify(inventoryStockRepository, Mockito.times(4)).findByProductId(fixture.product.id)
        Mockito.verify(categoryRepository, Mockito.times(2)).findAllByActiveTrueOrderByNameAsc()
    }
}
