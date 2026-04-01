package com.quickmart.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.integrations.mock-online-payment")
class MockOnlinePaymentProperties {
    var enabled: Boolean = false
    var baseUrl: String = "http://localhost:8089"
    var apiKey: String = "quickmart-local-sandbox-key"
    var connectTimeout: Duration = Duration.ofMillis(500)
    var readTimeout: Duration = Duration.ofSeconds(2)
}
