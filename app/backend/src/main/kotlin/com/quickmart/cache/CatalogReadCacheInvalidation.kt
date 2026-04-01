package com.quickmart.cache

import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.UUID

data class ProductReadCacheInvalidationEvent(
    val productId: UUID,
)

data object CategoryReadCacheInvalidationEvent

@Component
class CatalogReadCacheInvalidationPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun productChanged(productId: UUID) {
        applicationEventPublisher.publishEvent(ProductReadCacheInvalidationEvent(productId))
    }

    fun categoryChanged() {
        applicationEventPublisher.publishEvent(CategoryReadCacheInvalidationEvent)
    }
}

@Component
class CatalogReadCacheInvalidationListener(
    private val cacheManager: CacheManager,
    private val cacheKeyFactory: CacheKeyFactory,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProductChanged(event: ProductReadCacheInvalidationEvent) {
        evict(CacheNames.PUBLIC_PRODUCT_CARDS, cacheKeyFactory.publicProduct(event.productId))
        clear(CacheNames.PUBLIC_CATALOG_PAGES)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onCategoryChanged(@Suppress("UNUSED_PARAMETER") event: CategoryReadCacheInvalidationEvent) {
        clear(CacheNames.PUBLIC_CATEGORIES)
        clear(CacheNames.PUBLIC_PRODUCT_CARDS)
        clear(CacheNames.PUBLIC_CATALOG_PAGES)
    }

    private fun evict(
        cacheName: String,
        key: String,
    ) {
        cacheManager.getCache(cacheName)?.evict(key)
    }

    private fun clear(cacheName: String) {
        cacheManager.getCache(cacheName)?.clear()
    }
}
