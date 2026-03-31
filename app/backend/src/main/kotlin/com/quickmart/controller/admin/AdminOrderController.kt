package com.quickmart.controller.admin

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.FORBIDDEN_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADMIN_ORDERS
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.PageResponse
import com.quickmart.dto.admin.OrderEventAuditResponse
import com.quickmart.dto.order.OrderResponse
import com.quickmart.dto.order.OrderSummaryResponse
import com.quickmart.dto.order.UpdateOrderStatusRequest
import com.quickmart.service.OrderEventAuditService
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
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = TAG_ADMIN_ORDERS, description = "Операции административного управления заказами")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AdminOrderController(
    private val orderService: OrderService,
    private val orderEventAuditService: OrderEventAuditService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список заказов",
        description = "Назначение: Возвращает пагинированную коллекцию заказов для административной обработки.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: `page` использует нумерацию с нуля. `size` должен находиться в допустимом диапазоне валидации. `status` поддерживает значение `ACTIVE` или конкретное значение `OrderStatus`. `query` применяет текстовую фильтрацию согласно реализации сервиса.\nРезультат: Возвращает коллекцию кратких описаний заказов, отсортированную по времени создания в порядке убывания.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Пагинированная коллекция заказов возвращена."),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun all(
        @Parameter(description = "Идентифицирует индекс страницы с нумерацией от нуля. Формат: целое число. Минимальное значение — 0.", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Определяет размер страницы. Формат: целое число. Минимальное значение — 1. Максимальное значение — 200.", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(200) size: Int,
        @Parameter(description = "Определяет фильтр по статусу. Поддерживаемые значения: `ACTIVE` или конкретное имя `OrderStatus`.", example = "ACTIVE")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Определяет необязательный текстовый фильтр, интерпретируемый реализацией сервиса.", example = "Petrova")
        @RequestParam(required = false) query: String?,
    ): ResponseEntity<PageResponse<OrderSummaryResponse>> = ResponseEntity.ok(orderService.getAllOrders(page, size, status, query))

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить заказ",
        description = "Назначение: Возвращает ресурс заказа, идентифицированный переданным идентификатором.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Заказ должен существовать.\nРезультат: Возвращает ресурс заказа.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс заказа возвращен.", content = [Content(schema = Schema(implementation = OrderResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun details(
        @Parameter(description = "Идентифицирует ресурс заказа. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<OrderResponse> = ResponseEntity.ok(orderService.getOrderDetails(id))

    @GetMapping("/{id}/events")
    @Operation(
        summary = "Получить историю Kafka-событий заказа",
        description = "Назначение: Возвращает сохраненную историю доменных Kafka-событий по заказу, идентифицированному переданным идентификатором.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Заказ должен существовать. История может быть пустой, если Kafka-интеграция выключена или события по заказу еще не обработаны consumer-ом.\nРезультат: Возвращает коллекцию событий заказа в хронологическом порядке.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "История событий заказа возвращена.", content = [Content(schema = Schema(implementation = OrderEventAuditResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun orderEvents(
        @Parameter(description = "Идентифицирует ресурс заказа. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<List<OrderEventAuditResponse>> = ResponseEntity.ok(orderEventAuditService.getByOrderId(id))

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Обновить статус заказа",
        description = "Назначение: Переводит заказ, идентифицированный переданным идентификатором, в целевой статус.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Заказ должен существовать. Допустимы только следующие переходы: `CREATED -> CONFIRMED|CANCELLED`, `CONFIRMED -> ASSEMBLING|CANCELLED`, `ASSEMBLING -> OUT_FOR_DELIVERY`, `OUT_FOR_DELIVERY -> DELIVERED`. Любой иной переход возвращает HTTP 400.\nРезультат: Возвращает обновленный ресурс заказа.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс заказа обновлен.", content = [Content(schema = Schema(implementation = OrderResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun updateStatus(
        @Parameter(description = "Идентифицирует ресурс заказа. Формат: UUID.", example = "80000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевой статус заказа.")
        @Valid @RequestBody request: UpdateOrderStatusRequest,
    ): ResponseEntity<OrderResponse> = ResponseEntity.ok(orderService.updateStatus(id, request.status))
}
