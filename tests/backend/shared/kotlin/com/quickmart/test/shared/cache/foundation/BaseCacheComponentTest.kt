package com.quickmart.test.shared.cache.foundation

import com.quickmart.QuickmartApplication
import com.quickmart.repository.CategoryRepository
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.repository.ProductRepository
import com.quickmart.service.CategoryService
import com.quickmart.service.InventoryService
import com.quickmart.service.ProductService
import com.quickmart.test.shared.cache.scenario.CatalogCacheScenario
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cache.CacheManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [QuickmartApplication::class],
    properties = ["app.kafka.enabled=false"],
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
abstract class BaseCacheComponentTest {
    @Autowired
    protected lateinit var productService: ProductService

    @Autowired
    protected lateinit var categoryService: CategoryService

    @Autowired
    protected lateinit var inventoryService: InventoryService

    @Autowired
    protected lateinit var cacheManager: CacheManager

    @SpyBean
    protected lateinit var productRepository: ProductRepository

    @SpyBean
    protected lateinit var categoryRepository: CategoryRepository

    @SpyBean
    protected lateinit var inventoryStockRepository: InventoryStockRepository

    protected lateinit var catalogCacheScenario: CatalogCacheScenario

    @BeforeEach
    fun setUpCacheScenario() {
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
        catalogCacheScenario =
            CatalogCacheScenario(
                productService = productService,
                categoryService = categoryService,
                inventoryService = inventoryService,
            )
    }
}
