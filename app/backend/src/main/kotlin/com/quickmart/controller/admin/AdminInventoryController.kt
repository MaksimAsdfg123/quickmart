package com.quickmart.controller.admin

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.FORBIDDEN_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADMIN_INVENTORY
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.admin.InventoryStockResponse
import com.quickmart.dto.admin.InventoryUpdateRequest
import com.quickmart.service.InventoryService
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
@RequestMapping("/api/admin/inventory")
@Tag(name = TAG_ADMIN_INVENTORY, description = "Операции административного управления остатками")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AdminInventoryController(
    private val inventoryService: InventoryService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список остатков",
        description = "Назначение: Возвращает коллекцию складских остатков для административного управления запасами.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: `page` использует нумерацию с нуля. `size` должен находиться в допустимом диапазоне валидации.\nРезультат: Возвращает коллекцию складских остатков.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Коллекция складских остатков возвращена."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun all(
        @Parameter(description = "Идентифицирует индекс страницы с нумерацией от нуля. Формат: целое число. Минимальное значение — 0.", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Определяет размер страницы. Формат: целое число. Минимальное значение — 1. Максимальное значение — 200.", example = "50")
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<List<InventoryStockResponse>> = ResponseEntity.ok(inventoryService.getAll(page, size))

    @PutMapping("/{productId}")
    @Operation(
        summary = "Обновить остаток",
        description = "Назначение: Обновляет доступное количество товара для указанного продукта.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Товар должен существовать. Целевое количество должно быть неотрицательным.\nРезультат: Возвращает обновленный ресурс складского остатка.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс складского остатка обновлен.", content = [Content(schema = Schema(implementation = InventoryStockResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun update(
        @Parameter(description = "Идентифицирует ресурс товара. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable productId: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевое доступное количество.")
        @Valid @RequestBody request: InventoryUpdateRequest,
    ): ResponseEntity<InventoryStockResponse> = ResponseEntity.ok(inventoryService.updateStock(productId, request.availableQuantity))
}
