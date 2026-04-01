package com.quickmart.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "app.cache")
class AppCacheProperties {
    var enabled: Boolean = true
    var publicCatalogPages: CacheSpec = CacheSpec(Duration.ofSeconds(30), 256)
    var publicProductCards: CacheSpec = CacheSpec(Duration.ofSeconds(30), 1024)
    var publicCategories: CacheSpec = CacheSpec(Duration.ofMinutes(30), 64)

    class CacheSpec(
        var ttl: Duration = Duration.ofMinutes(5),
        var maximumSize: Long = 256,
    )
}
