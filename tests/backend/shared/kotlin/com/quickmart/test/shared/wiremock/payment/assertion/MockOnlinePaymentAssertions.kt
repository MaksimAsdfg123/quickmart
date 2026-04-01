package com.quickmart.test.shared.wiremock.payment.assertion

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.dto.order.OrderResponse
import com.quickmart.exception.BusinessException
import com.quickmart.repository.CartRepository
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.repository.OrderRepository
import com.quickmart.test.shared.foundation.allureStep
import com.quickmart.test.shared.wiremock.payment.data.MockOnlineCheckoutFixture
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID

object MockOnlinePaymentAssertions {
    fun assertApprovedCheckout(
        response: OrderResponse,
        fixture: MockOnlineCheckoutFixture,
    ) {
        allureStep("Проверить бизнес-результат успешной оплаты через внешний provider") {
            assertThat(response.status).isEqualTo(OrderStatus.CREATED)
            assertThat(response.paymentMethod).isEqualTo(PaymentMethod.MOCK_ONLINE)
            assertThat(response.paymentStatus).isEqualTo(PaymentStatus.PAID)
            assertThat(response.items).hasSize(1)
            assertThat(response.items.first().productId).isEqualTo(fixture.productId)
            assertThat(response.items.first().quantity).isEqualTo(fixture.quantity)
            assertThat(response.total).isEqualByComparingTo(fixture.expectedTotal)
            assertThat(response.subtotal).isEqualByComparingTo(fixture.expectedSubtotal)
        }
    }

    fun assertPersistedExternalReference(
        orderRepository: OrderRepository,
        orderId: UUID,
        expectedReference: String,
    ) {
        allureStep("Проверить сохранение external payment reference в заказе") {
            val savedOrder = orderRepository.findById(orderId).orElseThrow()
            assertThat(savedOrder.payment).isNotNull
            assertThat(savedOrder.payment!!.externalReference).isEqualTo(expectedReference)
        }
    }

    fun assertCartCleared(
        cartRepository: CartRepository,
        userId: UUID,
    ) {
        allureStep("Проверить очистку корзины после успешного checkout") {
            val cart = cartRepository.findByUserIdAndActiveTrue(userId).orElseThrow()
            assertThat(cart.items).isEmpty()
        }
    }

    fun assertCartRetained(
        cartRepository: CartRepository,
        userId: UUID,
        expectedQuantity: Int,
    ) {
        allureStep("Проверить сохранение корзины после неуспешного внешнего вызова") {
            val cart = cartRepository.findByUserIdAndActiveTrue(userId).orElseThrow()
            assertThat(cart.items).hasSize(1)
            assertThat(cart.items.first().quantity).isEqualTo(expectedQuantity)
        }
    }

    fun assertStockQuantity(
        inventoryStockRepository: InventoryStockRepository,
        productId: UUID,
        expectedQuantity: Int,
    ) {
        allureStep("Проверить итоговый складской остаток товара") {
            val stock = inventoryStockRepository.findByProductId(productId)
            assertThat(stock).isNotNull
            assertThat(stock!!.availableQuantity).isEqualTo(expectedQuantity)
        }
    }

    fun assertBusinessFailure(
        exception: Throwable,
        expectedStatus: Int,
        expectedMessagePart: String,
    ) {
        allureStep("Проверить контролируемое завершение сценария при ошибке внешней интеграции") {
            assertThat(exception).isInstanceOf(BusinessException::class.java)
            val businessException = exception as BusinessException
            assertThat(businessException.status).isEqualTo(expectedStatus)
            assertThat(businessException.message).contains(expectedMessagePart)
        }
    }
}
