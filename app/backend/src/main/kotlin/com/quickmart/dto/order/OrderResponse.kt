package com.quickmart.dto.order

import com.quickmart.domain.enums.OrderStatus
import com.quickmart.domain.enums.PaymentMethod
import com.quickmart.domain.enums.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Schema(description = "Представляет полный ресурс заказа.")
data class OrderResponse(
    @field:Schema(description = "Идентифицирует заказ. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Указывает текущий статус заказа.", example = "CREATED")
    val status: OrderStatus,
    @field:Schema(description = "Содержит неизменяемый снимок адреса, зафиксированный в момент создания заказа.", example = "Yekaterinburg, Lenina 10, apt. 25")
    val addressSnapshot: String,
    @field:Schema(description = "Содержит дату доставки.", example = "2026-03-27")
    val deliveryDate: LocalDate,
    @field:Schema(description = "Содержит время начала интервала доставки.", example = "10:00:00")
    val deliveryStartTime: LocalTime,
    @field:Schema(description = "Содержит время окончания интервала доставки.", example = "12:00:00")
    val deliveryEndTime: LocalTime,
    @field:Schema(description = "Содержит промокод, примененный к заказу, если он был использован.", example = "SAVE10")
    val promoCode: String?,
    @field:Schema(description = "Содержит промежуточную стоимость товаров до применения скидки и стоимости доставки.", example = "1290.00")
    val subtotal: BigDecimal,
    @field:Schema(description = "Содержит размер скидки, примененной к заказу.", example = "129.00")
    val discount: BigDecimal,
    @field:Schema(description = "Содержит стоимость доставки, примененную к заказу.", example = "149.00")
    val deliveryFee: BigDecimal,
    @field:Schema(description = "Содержит итоговую сумму к оплате по заказу.", example = "1310.00")
    val total: BigDecimal,
    @field:Schema(description = "Содержит заказанные позиции.")
    val items: List<OrderItemResponse>,
    @field:Schema(description = "Указывает способ оплаты, связанный с заказом.", example = "CARD")
    val paymentMethod: PaymentMethod,
    @field:Schema(description = "Указывает статус обработки оплаты.", example = "PAID")
    val paymentStatus: PaymentStatus,
    @field:Schema(description = "Содержит временную отметку создания заказа.", example = "2026-03-26T11:45:10")
    val createdAt: LocalDateTime,
)

@Schema(description = "Представляет заказанную позицию.")
data class OrderItemResponse(
    @field:Schema(description = "Идентифицирует позицию заказа. Формат: UUID.", example = "81000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Идентифицирует товар. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
    val productId: UUID,
    @field:Schema(description = "Содержит наименование товара, зафиксированное в снимке заказа.", example = "Bananas")
    val productName: String,
    @field:Schema(description = "Содержит цену единицы товара, зафиксированную в заказе.", example = "129.00")
    val unitPrice: BigDecimal,
    @field:Schema(description = "Указывает заказанное количество.", example = "2")
    val quantity: Int,
    @field:Schema(description = "Содержит итоговую сумму позиции.", example = "258.00")
    val lineTotal: BigDecimal,
)

@Schema(description = "Представляет краткий ресурс заказа.")
data class OrderSummaryResponse(
    @field:Schema(description = "Идентифицирует заказ. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Указывает текущий статус заказа.", example = "CREATED")
    val status: OrderStatus,
    @field:Schema(description = "Содержит итоговую сумму заказа.", example = "1310.00")
    val total: BigDecimal,
    @field:Schema(description = "Указывает способ оплаты.", example = "CARD")
    val paymentMethod: PaymentMethod,
    @field:Schema(description = "Указывает статус обработки оплаты.", example = "PAID")
    val paymentStatus: PaymentStatus,
    @field:Schema(description = "Указывает количество позиций заказа.", example = "3")
    val itemsCount: Int,
    @field:Schema(description = "Содержит дату доставки.", example = "2026-03-27")
    val deliveryDate: LocalDate,
    @field:Schema(description = "Содержит время начала интервала доставки.", example = "10:00:00")
    val deliveryStartTime: LocalTime,
    @field:Schema(description = "Содержит время окончания интервала доставки.", example = "12:00:00")
    val deliveryEndTime: LocalTime,
    @field:Schema(description = "Содержит временную отметку создания заказа.", example = "2026-03-26T11:45:10")
    val createdAt: LocalDateTime,
)
