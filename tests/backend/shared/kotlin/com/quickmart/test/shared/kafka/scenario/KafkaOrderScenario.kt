package com.quickmart.test.shared.kafka.scenario

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
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.Role
import com.quickmart.dto.admin.OrderEventAuditResponse
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.dto.order.OrderResponse
import com.quickmart.events.OrderEventItemPayload
import com.quickmart.events.OrderEventPayload
import com.quickmart.events.OrderLifecycleIntegrationEvent
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
import com.quickmart.test.shared.common.util.RandomDataUtils
import com.quickmart.test.shared.foundation.allureStep
import com.quickmart.test.shared.kafka.data.CheckoutFixture
import com.quickmart.test.shared.kafka.data.CheckoutFixtureOptions
import com.quickmart.test.shared.kafka.data.CheckoutScenarioResult
import com.quickmart.test.shared.kafka.data.KafkaTestDataFactory
import com.quickmart.test.shared.kafka.data.ManualKafkaEventSpec
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class KafkaOrderScenario(
    private val checkoutService: CheckoutService,
    private val orderService: OrderService,
    private val orderEventAuditService: OrderEventAuditService,
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val inventoryStockRepository: InventoryStockRepository,
    private val deliverySlotRepository: DeliverySlotRepository,
    private val cartRepository: CartRepository,
    private val orderEventAuditRepository: OrderEventAuditRepository,
    private val orderRepository: OrderRepository,
    private val objectMapper: ObjectMapper,
) {
    fun checkoutCardOrder(
        prefix: String = "card-order",
        options: CheckoutFixtureOptions = KafkaTestDataFactory.defaultCheckoutOptions(),
    ): CheckoutScenarioResult = checkoutOrder(prefix, PaymentMethod.CARD, options)

    fun checkoutCashOrder(
        prefix: String = "cash-order",
        options: CheckoutFixtureOptions = KafkaTestDataFactory.defaultCheckoutOptions(),
    ): CheckoutScenarioResult = checkoutOrder(prefix, PaymentMethod.CASH, options)

    fun checkoutMockOnlineOrder(
        prefix: String = "mock-online-order",
        options: CheckoutFixtureOptions = KafkaTestDataFactory.defaultCheckoutOptions(),
    ): CheckoutScenarioResult = checkoutOrder(prefix, PaymentMethod.MOCK_ONLINE, options)

    fun createCheckoutFixture(
        prefix: String,
        options: CheckoutFixtureOptions = KafkaTestDataFactory.defaultCheckoutOptions(),
    ): CheckoutFixture =
        allureStep("Подготовить checkout fixture для Kafka-сценария: $prefix") {
            val user =
                userRepository.save(
                    User().apply {
                        email = RandomDataUtils.uniqueEmail("qa.kafka.$prefix")
                        passwordHash = "password"
                        fullName = RandomDataUtils.uniqueName("Kafka Customer")
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
                        name = RandomDataUtils.uniqueName("Kafka Category $prefix")
                        description = "Kafka test category"
                        active = true
                    },
                )

            val product =
                productRepository.save(
                    Product().apply {
                        name = RandomDataUtils.uniqueName("Kafka Product $prefix")
                        description = "Kafka test product"
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

            CheckoutFixture(
                user = user,
                address = address,
                deliverySlot = deliverySlot,
                product = product,
                quantity = options.quantity,
                unitPrice = product.price,
            )
        }

    fun checkoutExpensiveMockOnlineOrder(prefix: String = "expensive-online"): CheckoutScenarioResult =
        checkoutMockOnlineOrder(prefix, KafkaTestDataFactory.expensiveCheckoutOptions())

    fun confirmOrder(orderId: UUID): OrderResponse =
        allureStep("Подтвердить заказ $orderId") {
            orderService.updateStatus(orderId, OrderStatus.CONFIRMED)
        }

    fun updateStatus(
        orderId: UUID,
        targetStatus: OrderStatus,
    ): OrderResponse =
        allureStep("Перевести заказ $orderId в статус $targetStatus") {
            orderService.updateStatus(orderId, targetStatus)
        }

    fun cancelAsCustomer(
        userId: UUID,
        orderId: UUID,
    ): OrderResponse =
        allureStep("Отменить заказ $orderId от имени покупателя") {
            orderService.cancelMyOrder(userId, orderId)
        }

    fun cancelAsAdmin(orderId: UUID): OrderResponse =
        allureStep("Отменить заказ $orderId через admin status update") {
            updateStatus(orderId, OrderStatus.CANCELLED)
        }

    fun deliverCashOrder(orderId: UUID): OrderResponse =
        allureStep("Довести cash-заказ $orderId до статуса DELIVERED") {
            updateStatus(orderId, OrderStatus.CONFIRMED)
            updateStatus(orderId, OrderStatus.ASSEMBLING)
            updateStatus(orderId, OrderStatus.OUT_FOR_DELIVERY)
            updateStatus(orderId, OrderStatus.DELIVERED)
        }

    fun awaitAuditTrail(
        orderId: UUID,
        expectedCount: Int,
    ): List<OrderEventAudit> =
        allureStep("Дождаться $expectedCount audit entries для заказа $orderId") {
            await.atMost(Duration.ofSeconds(10)).untilAsserted {
                assertThat(currentAuditTrail(orderId)).hasSize(expectedCount)
            }
            currentAuditTrail(orderId)
        }

    fun currentAuditTrail(orderId: UUID): List<OrderEventAudit> =
        orderEventAuditRepository.findAllByOrderIdOrderByOccurredAtAscCreatedAtAsc(orderId)

    fun readAuditHistory(orderId: UUID): List<OrderEventAuditResponse> =
        allureStep("Прочитать audit history по заказу $orderId") {
            orderEventAuditService.getByOrderId(orderId)
        }

    fun payloadOf(audit: OrderEventAudit): JsonNode =
        allureStep("Распарсить payload audit entry ${audit.eventId}") {
            objectMapper.readTree(audit.payloadJson)
        }

    fun orderCount(): Long = orderRepository.count()

    fun auditCount(): Long = orderEventAuditRepository.count()

    fun recordEvent(event: OrderLifecycleIntegrationEvent) {
        allureStep("Сохранить Kafka audit event ${event.eventId}") {
            orderEventAuditService.record(event)
        }
    }

    fun recordDuplicateEvent(event: OrderLifecycleIntegrationEvent) {
        allureStep("Повторно обработать один и тот же Kafka event ${event.eventId}") {
            orderEventAuditService.record(event)
            orderEventAuditService.record(event)
        }
    }

    fun buildManualEvent(
        fixture: CheckoutFixture,
        orderId: UUID,
        spec: ManualKafkaEventSpec,
    ): OrderLifecycleIntegrationEvent =
        allureStep("Собрать manual Kafka event ${spec.eventType.code} для заказа $orderId") {
            OrderLifecycleIntegrationEvent(
                eventId = spec.eventId,
                eventType = spec.eventType.code,
                aggregateId = orderId,
                occurredAt = spec.occurredAt,
                payloadVersion = 1,
                payload =
                    OrderEventPayload(
                        orderId = orderId,
                        userId = fixture.user.id!!,
                        previousStatus = spec.previousStatus,
                        currentStatus = spec.currentStatus,
                        paymentMethod = spec.paymentMethod,
                        paymentStatus = spec.paymentStatus,
                        total = spec.total,
                        itemCount = fixture.quantity,
                        items =
                            listOf(
                                OrderEventItemPayload(
                                    productId = fixture.product.id!!,
                                    productName = fixture.product.name,
                                    quantity = fixture.quantity,
                                ),
                            ),
                    ),
            )
        }

    private fun checkoutOrder(
        prefix: String,
        paymentMethod: PaymentMethod,
        options: CheckoutFixtureOptions,
    ): CheckoutScenarioResult =
        allureStep("Выполнить checkout с оплатой $paymentMethod для сценария $prefix") {
            val fixture = createCheckoutFixture(prefix, options)
            val order =
                checkoutService.checkout(
                    fixture.user.id!!,
                    CheckoutRequest(
                        addressId = fixture.address.id!!,
                        deliverySlotId = fixture.deliverySlot.id!!,
                        promoCode = null,
                        paymentMethod = paymentMethod,
                    ),
                )
            CheckoutScenarioResult(
                fixture = fixture,
                order = order,
            )
        }
}
