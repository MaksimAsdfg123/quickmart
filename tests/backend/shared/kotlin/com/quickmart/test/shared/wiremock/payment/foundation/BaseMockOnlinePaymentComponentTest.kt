package com.quickmart.test.shared.wiremock.payment.foundation

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.quickmart.QuickmartApplication
import com.quickmart.repository.CartRepository
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.repository.OrderRepository
import com.quickmart.repository.PaymentRepository
import com.quickmart.service.AddressService
import com.quickmart.service.AuthService
import com.quickmart.service.CartService
import com.quickmart.service.CategoryService
import com.quickmart.service.CheckoutService
import com.quickmart.service.DeliverySlotService
import com.quickmart.service.InventoryService
import com.quickmart.service.ProductService
import com.quickmart.test.shared.wiremock.payment.scenario.MockOnlinePaymentCheckoutScenario
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(
    classes = [QuickmartApplication::class],
    properties = [
        "app.kafka.enabled=false",
        "app.cache.enabled=false",
    ],
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
abstract class BaseMockOnlinePaymentComponentTest {
    @Autowired
    protected lateinit var authService: AuthService

    @Autowired
    protected lateinit var categoryService: CategoryService

    @Autowired
    protected lateinit var productService: ProductService

    @Autowired
    protected lateinit var inventoryService: InventoryService

    @Autowired
    protected lateinit var addressService: AddressService

    @Autowired
    protected lateinit var deliverySlotService: DeliverySlotService

    @Autowired
    protected lateinit var cartService: CartService

    @Autowired
    protected lateinit var checkoutService: CheckoutService

    @Autowired
    protected lateinit var orderRepository: OrderRepository

    @Autowired
    protected lateinit var paymentRepository: PaymentRepository

    @Autowired
    protected lateinit var cartRepository: CartRepository

    @Autowired
    protected lateinit var inventoryStockRepository: InventoryStockRepository

    protected lateinit var checkoutScenario: MockOnlinePaymentCheckoutScenario

    @BeforeEach
    fun setUpWireMockScenario() {
        wireMockServer.resetAll()
        checkoutScenario =
            MockOnlinePaymentCheckoutScenario(
                authService = authService,
                categoryService = categoryService,
                productService = productService,
                inventoryService = inventoryService,
                addressService = addressService,
                deliverySlotService = deliverySlotService,
                cartService = cartService,
                checkoutService = checkoutService,
            )
    }

    companion object {
        const val PAYMENT_PROVIDER_API_KEY = "quickmart-wiremock-api-key"

        @JvmStatic
        protected val wireMockServer =
            WireMockServer(wireMockConfig().dynamicPort()).apply {
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerMockOnlinePaymentProperties(registry: DynamicPropertyRegistry) {
            registry.add("app.integrations.mock-online-payment.enabled") { true }
            registry.add("app.integrations.mock-online-payment.base-url") { wireMockServer.baseUrl() }
            registry.add("app.integrations.mock-online-payment.api-key") { PAYMENT_PROVIDER_API_KEY }
            registry.add("app.integrations.mock-online-payment.connect-timeout") { "300ms" }
            registry.add("app.integrations.mock-online-payment.read-timeout") { "300ms" }
        }

        @JvmStatic
        @AfterAll
        fun stopWireMockServer() {
            wireMockServer.stop()
        }
    }
}
