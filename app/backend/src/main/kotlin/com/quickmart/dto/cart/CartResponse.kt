package com.quickmart.dto.cart

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Представляет текущее состояние корзины аутентифицированного пользователя.")
data class CartResponse(
    @field:Schema(description = "Идентифицирует активную корзину. Формат: UUID.", example = "10000000-0000-0000-0000-000000000002")
    val id: UUID,
    @field:Schema(description = "Содержит текущие позиции корзины.")
    val items: List<CartItemResponse>,
    @field:Schema(description = "Указывает совокупное количество единиц товара по всем позициям корзины.", example = "3")
    val totalItems: Int,
    @field:Schema(description = "Содержит промежуточную стоимость товаров до применения скидки и стоимости доставки.", example = "387.00")
    val subtotal: BigDecimal,
)

@Schema(description = "Представляет отдельную позицию корзины.")
data class CartItemResponse(
    @field:Schema(description = "Идентифицирует позицию корзины. Формат: UUID.", example = "90000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Идентифицирует товар. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
    val productId: UUID,
    @field:Schema(description = "Содержит наименование товара.", example = "Bananas")
    val productName: String,
    @field:Schema(description = "Содержит цену единицы товара, примененную к позиции корзины.", example = "129.00")
    val unitPrice: BigDecimal,
    @field:Schema(description = "Указывает количество товара в корзине.", example = "2")
    val quantity: Int,
    @field:Schema(description = "Содержит стоимость позиции корзины.", example = "258.00")
    val lineTotal: BigDecimal,
    @field:Schema(description = "Указывает текущее доступное количество товара на складе.", example = "50")
    val availableQuantity: Int,
)

