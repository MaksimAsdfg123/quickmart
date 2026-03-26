package com.quickmart.controller

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ORDERS
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.PageResponse
import com.quickmart.dto.order.CheckoutRequest
import com.quickmart.dto.order.OrderResponse
import com.quickmart.dto.order.OrderSummaryResponse
import com.quickmart.security.AuthFacade
import com.quickmart.service.CheckoutService
import com.quickmart.service.OrderService
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
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/orders")
@Tag(name = TAG_ORDERS, description = "Операции оформления и просмотра заказов пользователя")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class OrderController(
    private val checkoutService: CheckoutService,
    private val orderService: OrderService,
    private val authFacade: AuthFacade,
) {
    @PostMapping("/checkout")
    @Operation(
        summary = "Создать заказ",
        description = "Назначение: Создает заказ из активной корзины аутентифицированного пользователя.\nАвторизация: Bearer JWT.\nОграничения: Корзина не должна быть пустой. Адрес должен принадлежать аутентифицированному пользователю. Слот доставки должен существовать, быть активным, не относиться к прошлому и иметь свободную емкость. Промокод, если он передан, должен удовлетворять всем правилам валидации. Способ оплаты `MOCK_ONLINE` завершается ошибкой, если итоговая сумма превышает 50000.\nРезультат: Возвращает созданный ресурс заказа и очищает активную корзину.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ресурс заказа создан.", content = [Content(schema = Schema(implementation = OrderResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun checkout(
        @Parameter(hidden = true) authentication: Authentication,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая адрес доставки, слот доставки, необязательный промокод и способ оплаты.")
        @Valid @RequestBody request: CheckoutRequest,
    ): ResponseEntity<OrderResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutService.checkout(userId, request))
    }

    @GetMapping
    @Operation(
        summary = "Получить список заказов",
        description = "Назначение: Возвращает пагинированную коллекцию заказов, принадлежащих аутентифицированному пользователю.\nАвторизация: Bearer JWT.\nОграничения: `page` использует нумерацию с нуля. `size` должен находиться в допустимом диапазоне валидации.\nРезультат: Возвращает коллекцию кратких описаний заказов, отсортированную по времени создания в порядке убывания.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Пагинированная коллекция заказов возвращена."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun myOrders(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует индекс страницы с нумерацией от нуля. Формат: целое число. Минимальное значение — 0.", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Определяет размер страницы. Формат: целое число. Минимальное значение — 1. Максимальное значение — 200.", example = "10")
        @RequestParam(defaultValue = "10") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<PageResponse<OrderSummaryResponse>> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(orderService.getMyOrders(userId, page, size))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить заказ",
        description = "Назначение: Возвращает ресурс заказа, идентифицированный переданным идентификатором, если заказ принадлежит аутентифицированному пользователю.\nАвторизация: Bearer JWT.\nОграничения: Заказ должен существовать и принадлежать аутентифицированному пользователю.\nРезультат: Возвращает ресурс заказа.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс заказа возвращен.", content = [Content(schema = Schema(implementation = OrderResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun myOrderDetails(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует ресурс заказа. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<OrderResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(orderService.getMyOrderDetails(userId, id))
    }

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Отменить заказ",
        description = "Назначение: Отменяет заказ, идентифицированный переданным идентификатором.\nАвторизация: Bearer JWT.\nОграничения: Заказ должен принадлежать аутентифицированному пользователю. Отмена со стороны пользователя допускается только для заказов в статусе `CREATED` или `CONFIRMED`.\nРезультат: Возвращает обновленный ресурс заказа.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс заказа обновлен.", content = [Content(schema = Schema(implementation = OrderResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun cancel(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует ресурс заказа. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<OrderResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(orderService.cancelMyOrder(userId, id))
    }
}


