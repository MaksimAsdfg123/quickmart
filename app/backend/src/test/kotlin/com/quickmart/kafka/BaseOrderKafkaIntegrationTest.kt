package com.quickmart.kafka

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.domain.entity.Address
import com.quickmart.domain.entity.Cart
import com.quickmart.domain.entity.CartItem
import com.quickmart.domain.entity.Category
import com.quickmart.domain.entity.DeliverySlot
import com.quickmart.domain.entity.InventoryStock
import com.quickmart.domain.entity.OrderEventAudit
import com.quickmart.domain.entity.Product
import com.quickmart.domain.entity.User
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.dto.order.OrderResponse
import com.quickmart.domain.enums.Role
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
import com.quickmart.service.OrderService
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration
import java.util.UUID

abstract class BaseOrderKafkaIntegrationTest {
    @Autowired
    protected lateinit var checkoutService: CheckoutService

    @Autowired
    protected lateinit var orderService: OrderService

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

    protected fun createCheckoutFixture(
        prefix: String,
        options: CheckoutFixtureOptions = CheckoutFixtureOptions(),
    ): CheckoutFixture {
        val user =
            userRepository.save(
                User().apply {
                    email = "$prefix-${System.nanoTime()}@quickmart.local"
                    passwordHash = "password"
                    fullName = "Kafka Test Customer"
                    role = Role.CUSTOMER
                    active = true
                },
            )

        val address =
            addressRepository.save(
                Address().apply {
                    this.user = user
                    label = "Home"
                    city = "Yekaterinburg"
                    street = "Lenina"
                    house = "1"
                    apartment = "10"
                    isDefault = true
                },
            )

        val category =
            categoryRepository.save(
                Category().apply {
                    name = "Category $prefix ${System.nanoTime()}"
                    description = "Test category"
                    active = true
                },
            )

        val product =
            productRepository.save(
                Product().apply {
                    name = "Product $prefix ${System.nanoTime()}"
                    description = "Test product"
                    price = options.unitPrice
                    this.category = category
                    active = true
                },
            )

        inventoryStockRepository.save(
            InventoryStock().apply {
                this.product = product
                availableQuantity = 25
            },
        )

        val deliverySlot =
            deliverySlotRepository.save(
                DeliverySlot().apply {
                    slotDate = LocalDate.now().plusDays(1)
                    startTime = LocalTime.of(10, 0)
                    endTime = LocalTime.of(12, 0)
                    orderLimit = 10
                    active = true
                },
            )

        val cart =
            Cart().apply {
                this.user = user
                active = true
            }

        if (options.withCartItems) {
            cart.items.add(
                CartItem().apply {
                    this.cart = cart
                    this.product = product
                    quantity = options.quantity
                },
            )
        }

        cartRepository.save(cart)
        return CheckoutFixture(
            user = user,
            address = address,
            deliverySlot = deliverySlot,
            product = product,
            quantity = options.quantity,
            unitPrice = product.price,
        )
    }

    protected fun createExpensiveCheckoutFixture(prefix: String): CheckoutFixture =
        createCheckoutFixture(
            prefix,
            CheckoutFixtureOptions(
                unitPrice = BigDecimal("60000.00"),
                quantity = 1,
            ),
        )

    protected fun checkout(
        fixture: CheckoutFixture,
        paymentMethod: PaymentMethod = PaymentMethod.CARD,
    ): OrderResponse =
        checkoutService.checkout(
            fixture.user.id!!,
            CheckoutRequest(
                addressId = fixture.address.id!!,
                deliverySlotId = fixture.deliverySlot.id!!,
                promoCode = null,
                paymentMethod = paymentMethod,
            ),
        )

    protected fun currentAudits(orderId: UUID): List<OrderEventAudit> =
        orderEventAuditRepository.findAllByOrderIdOrderByOccurredAtAscCreatedAtAsc(orderId)

    protected fun awaitAudits(
        orderId: UUID,
        expectedCount: Int,
    ): List<OrderEventAudit> {
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            assertThat(currentAudits(orderId)).hasSize(expectedCount)
        }
        return currentAudits(orderId)
    }

    protected fun payloadOf(audit: OrderEventAudit): JsonNode = objectMapper.readTree(audit.payloadJson)

    protected data class CheckoutFixtureOptions(
        val unitPrice: BigDecimal = BigDecimal("499.00"),
        val quantity: Int = 2,
        val withCartItems: Boolean = true,
    )

    protected data class CheckoutFixture(
        val user: User,
        val address: Address,
        val deliverySlot: DeliverySlot,
        val product: Product,
        val quantity: Int,
        val unitPrice: BigDecimal,
    )
}
