package com.quickmart.controller

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_CART
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.cart.AddCartItemRequest
import com.quickmart.dto.cart.CartResponse
import com.quickmart.dto.cart.UpdateCartItemRequest
import com.quickmart.security.AuthFacade
import com.quickmart.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/cart")
@Tag(name = TAG_CART, description = "Операции с корзиной пользователя")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class CartController(
    private val cartService: CartService,
    private val authFacade: AuthFacade,
) {
    @GetMapping
    @Operation(
        summary = "Получить корзину",
        description = "Назначение: Возвращает текущую активную корзину аутентифицированного субъекта.\nАвторизация: Bearer JWT.\nОграничения: Корзина создается неявно при ее отсутствии.\nРезультат: Возвращает текущее состояние корзины.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Текущее состояние корзины возвращено.", content = [Content(schema = Schema(implementation = CartResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun getCart(
        @Parameter(hidden = true) authentication: Authentication,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.getCart(authFacade.userId(authentication)))

    @PostMapping("/items")
    @Operation(
        summary = "Добавить позицию корзины",
        description = "Назначение: Добавляет товар в активную корзину либо увеличивает количество существующей позиции.\nАвторизация: Bearer JWT.\nОграничения: Товар должен существовать, быть активным и иметь достаточный доступный остаток для итогового количества.\nРезультат: Возвращает обновленное состояние корзины.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Состояние корзины обновлено.", content = [Content(schema = Schema(implementation = CartResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun addItem(
        @Parameter(hidden = true) authentication: Authentication,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая идентификатор товара и количество для добавления.")
        @Valid @RequestBody request: AddCartItemRequest,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.addItem(authFacade.userId(authentication), request))

    @PutMapping("/items/{productId}")
    @Operation(
        summary = "Обновить позицию корзины",
        description = "Назначение: Устанавливает целевое количество для указанной позиции корзины.\nАвторизация: Bearer JWT.\nОграничения: Позиция корзины должна существовать. Количество `0` удаляет позицию. Для положительных значений товар должен оставаться активным, а остаток должен быть достаточным.\nРезультат: Возвращает обновленное состояние корзины.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Состояние корзины обновлено.", content = [Content(schema = Schema(implementation = CartResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun updateItem(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует товар, связанный с позицией корзины. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable productId: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевое количество позиции корзины.")
        @Valid @RequestBody request: UpdateCartItemRequest,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.updateItem(authFacade.userId(authentication), productId, request))

    @DeleteMapping("/items/{productId}")
    @Operation(
        summary = "Удалить позицию корзины",
        description = "Назначение: Удаляет указанный товар из активной корзины.\nАвторизация: Bearer JWT.\nОграничения: Операция идемпотентна относительно отсутствия товара в корзине.\nРезультат: Возвращает обновленное состояние корзины.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Состояние корзины обновлено.", content = [Content(schema = Schema(implementation = CartResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun removeItem(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует товар, связанный с позицией корзины. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable productId: UUID,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.removeItem(authFacade.userId(authentication), productId))
}

