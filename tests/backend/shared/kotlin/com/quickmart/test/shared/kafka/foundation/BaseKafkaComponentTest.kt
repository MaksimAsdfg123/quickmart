package com.quickmart.test.shared.kafka.foundation

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.repository.AddressRepository
import com.quickmart.repository.CartRepository
import com.quickmart.repository.CategoryRepository
import com.quickmart.repository.DeliverySlotRepository
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.repository.OrderEventAuditRepository
import com.quickmart.repository.OrderRepository
import com.quickmart.repository.ProductRepository
import com.quickmart.repository.UserRepository
import com.quickmart.service.CheckoutService
import com.quickmart.service.OrderEventAuditService
import com.quickmart.service.OrderService
import com.quickmart.test.shared.kafka.scenario.KafkaOrderScenario
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseKafkaComponentTest {
    @Autowired
    protected lateinit var checkoutService: CheckoutService

    @Autowired
    protected lateinit var orderService: OrderService

    @Autowired
    protected lateinit var orderEventAuditService: OrderEventAuditService

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var addressRepository: AddressRepository

    @Autowired
    protected lateinit var categoryRepository: CategoryRepository

    @Autowired
    protected lateinit var productRepository: ProductRepository

    @Autowired
    protected lateinit var inventoryStockRepository: InventoryStockRepository

    @Autowired
    protected lateinit var deliverySlotRepository: DeliverySlotRepository

    @Autowired
    protected lateinit var cartRepository: CartRepository

    @Autowired
    protected lateinit var orderEventAuditRepository: OrderEventAuditRepository

    @Autowired
    protected lateinit var orderRepository: OrderRepository

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected lateinit var kafkaOrderScenario: KafkaOrderScenario

    @BeforeEach
    fun baseKafkaSetup() {
        kafkaOrderScenario =
            KafkaOrderScenario(
                checkoutService = checkoutService,
                orderService = orderService,
                orderEventAuditService = orderEventAuditService,
                userRepository = userRepository,
                addressRepository = addressRepository,
                categoryRepository = categoryRepository,
                productRepository = productRepository,
                inventoryStockRepository = inventoryStockRepository,
                deliverySlotRepository = deliverySlotRepository,
                cartRepository = cartRepository,
                orderEventAuditRepository = orderEventAuditRepository,
                orderRepository = orderRepository,
                objectMapper = objectMapper,
            )
    }
}
