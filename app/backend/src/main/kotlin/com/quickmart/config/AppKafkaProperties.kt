package com.quickmart.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.kafka")
class AppKafkaProperties {
    var enabled: Boolean = false
    var topic: String = "quickmart.order-events"
    var consumerGroup: String = "quickmart-order-audit"
}
