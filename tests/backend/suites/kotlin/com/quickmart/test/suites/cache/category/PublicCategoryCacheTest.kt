package com.quickmart.test.suites.cache.category

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

@Epic("Quickmart Backend Tests")
@Feature("Cache Integration")
@Owner("backend-platform")
@Tag("cache")
@DisplayName("Cache: public categories")
class PublicCategoryCacheTest : BaseCacheComponentTest() {
    @Test
    @Story("Reference data")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Повторное чтение публичных категорий использует кэш и после deactivate кэш инвалидируется")
    fun shouldCachePublicCategoriesAndEvictAfterCategoryMutation() {
        val fixture = catalogCacheScenario.createCatalogFixture(prefix = "public-categories")
        Mockito.clearInvocations(categoryRepository)

        val first = catalogCacheScenario.fetchPublicCategories()
        val second = catalogCacheScenario.fetchPublicCategories()

        CacheAssertions.assertPublicCategories(first, setOf(fixture.category.name))
        CacheAssertions.assertPublicCategories(second, setOf(fixture.category.name))
        Mockito.verify(categoryRepository, Mockito.times(1)).findAllByActiveTrueOrderByNameAsc()

        catalogCacheScenario.deactivateCategory(fixture)
        Mockito.clearInvocations(categoryRepository)

        val refreshed = catalogCacheScenario.fetchPublicCategories()

        CacheAssertions.assertPublicCategories(refreshed, emptySet())
        Mockito.verify(categoryRepository, Mockito.times(1)).findAllByActiveTrueOrderByNameAsc()
    }
}
