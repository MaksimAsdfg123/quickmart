package com.quickmart.service

import com.quickmart.domain.entity.Order
import com.quickmart.domain.entity.OrderItem
import com.quickmart.domain.entity.Payment
import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.dto.order.OrderResponse
import com.quickmart.exception.BusinessException
import com.quickmart.mapper.OrderMapper
import com.quickmart.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class CheckoutService(
    private val cartService: CartService,
    private val addressService: AddressService,
    private val deliverySlotService: DeliverySlotService,
    private val promoCodeService: PromoCodeService,
    private val inventoryService: InventoryService,
    private val orderRepository: OrderRepository,
    private val orderMapper: OrderMapper,
) {
    private val freeDeliveryThreshold = BigDecimal("1500.00")
    private val baseDeliveryFee = BigDecimal("149.00")

    @Transactional
    fun checkout(
        userId: UUID,
        request: CheckoutRequest,
    ): OrderResponse {
        val cart = cartService.getOrCreateCart(userId)
        if (cart.items.isEmpty()) {
            throw BusinessException("Корзина пуста")
        }

        val address = addressService.getByIdAndUserOrThrow(request.addressId, userId)
        val deliverySlot = deliverySlotService.lockAndValidateForCheckout(request.deliverySlotId)

        val subtotal =
            cart.items.fold(BigDecimal.ZERO) { acc, item ->
                acc + item.product.price.multiply(item.quantity.toBigDecimal())
            }

        val promo = promoCodeService.resolveAndValidate(request.promoCode, subtotal)
        val discount = promoCodeService.calculateDiscount(subtotal, promo)
        val deliveryFee = if (subtotal >= freeDeliveryThreshold) BigDecimal.ZERO else baseDeliveryFee
        val total = subtotal.subtract(discount).add(deliveryFee).max(BigDecimal.ZERO)

        val order =
            Order().apply {
                user = address.user
                this.address = address
                this.deliverySlot = deliverySlot
                this.promoCode = promo
                status = OrderStatus.CREATED
                this.subtotal = subtotal
                this.discount = discount
                this.deliveryFee = deliveryFee
                this.total = total
                addressSnapshot = address.toSingleLine()
            }

        cart.items.forEach { cartItem ->
            if (!cartItem.product.active) {
                throw BusinessException("Товар ${cartItem.product.name} недоступен")
            }

            inventoryService.decreaseStockWithLock(cartItem.product.id!!, cartItem.quantity)

            val orderItem =
                OrderItem().apply {
                    this.order = order
                    this.product = cartItem.product
                    productName = cartItem.product.name
                    unitPrice = cartItem.product.price
                    quantity = cartItem.quantity
                    lineTotal = unitPrice.multiply(quantity.toBigDecimal())
                }
            order.items.add(orderItem)
        }

        val payment =
            Payment().apply {
                this.order = order
                method = request.paymentMethod
                status = resolvePaymentStatus(request.paymentMethod, total)
                amount = total
                if (request.paymentMethod == PaymentMethod.MOCK_ONLINE) {
                    externalReference = "MOCK-${System.currentTimeMillis()}"
                }
            }

        if (payment.status == PaymentStatus.FAILED) {
            throw BusinessException("Оплата не прошла")
        }

        order.payment = payment

        val savedOrder = orderRepository.save(order)

        if (promo != null) {
            promoCodeService.incrementUsage(promo)
        }

        cart.items.clear()

        return orderMapper.toResponse(savedOrder)
    }

    private fun resolvePaymentStatus(
        method: PaymentMethod,
        total: BigDecimal,
    ): PaymentStatus =
        when (method) {
            PaymentMethod.CASH -> PaymentStatus.PENDING
            PaymentMethod.CARD -> PaymentStatus.PAID
            PaymentMethod.MOCK_ONLINE -> {
                if (total > BigDecimal("50000")) PaymentStatus.FAILED else PaymentStatus.PAID
            }
        }
}
