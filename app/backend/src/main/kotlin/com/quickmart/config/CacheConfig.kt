package com.quickmart.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.quickmart.cache.CacheNames
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig(
    private val cacheProperties: AppCacheProperties,
) {
    @Bean
    fun cacheManager(): CacheManager {
        if (!cacheProperties.enabled) {
            return NoOpCacheManager()
        }

        val delegate =
            SimpleCacheManager().apply {
                setCaches(
                    listOf(
                        buildCache(CacheNames.PUBLIC_CATALOG_PAGES, cacheProperties.publicCatalogPages),
                        buildCache(CacheNames.PUBLIC_PRODUCT_CARDS, cacheProperties.publicProductCards),
                        buildCache(CacheNames.PUBLIC_CATEGORIES, cacheProperties.publicCategories),
                    ),
                )
                initializeCaches()
            }

        return TransactionAwareCacheManagerProxy(delegate)
    }

    private fun buildCache(
        name: String,
        spec: AppCacheProperties.CacheSpec,
    ): Cache =
        CaffeineCache(
            name,
            Caffeine
                .newBuilder()
                .maximumSize(spec.maximumSize)
                .expireAfterWrite(spec.ttl)
                .recordStats()
                .build<Any, Any>(),
            false,
        )
}
