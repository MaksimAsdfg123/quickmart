package com.quickmart.controller.admin

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.CONFLICT_RESPONSE_REF
import com.quickmart.config.FORBIDDEN_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADMIN_PRODUCTS
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.PageResponse
import com.quickmart.dto.product.ProductRequest
import com.quickmart.dto.product.ProductResponse
import com.quickmart.service.ProductService
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
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/admin/products")
@Tag(name = TAG_ADMIN_PRODUCTS, description = "Операции административного управления товарами")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AdminProductController(
    private val productService: ProductService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список товаров",
        description = "Назначение: Возвращает пагинированную коллекцию товаров для административной обработки.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: `page` использует нумерацию с нуля. `size` должен находиться в допустимом диапазоне валидации. В выборку включаются как активные, так и неактивные товары.\nРезультат: Возвращает коллекцию товаров, отсортированную согласно реализации сервиса.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Пагинированная коллекция товаров возвращена."),
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
    ): ResponseEntity<PageResponse<ProductResponse>> = ResponseEntity.ok(productService.getAllForAdmin(page, size))

    @PostMapping
    @Operation(
        summary = "Создать товар",
        description = "Назначение: Создает новый ресурс товара.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Указанная категория должна существовать. Полезная нагрузка запроса должна удовлетворять ограничениям валидации. Связанная запись складского остатка инициализируется сервисом со значением количества, равным нулю.\nРезультат: Возвращает созданный ресурс товара.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ресурс товара создан.", content = [Content(schema = Schema(implementation = ProductResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun create(
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая атрибуты создаваемого товара.")
        @Valid @RequestBody request: ProductRequest,
    ): ResponseEntity<ProductResponse> = ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request))

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить товар",
        description = "Назначение: Обновляет ресурс товара, идентифицированный переданным идентификатором.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Товар и указанная категория должны существовать. Полезная нагрузка запроса должна удовлетворять ограничениям валидации.\nРезультат: Возвращает обновленный ресурс товара.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс товара обновлен.", content = [Content(schema = Schema(implementation = ProductResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun update(
        @Parameter(description = "Идентифицирует ресурс товара. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевые атрибуты товара.")
        @Valid @RequestBody request: ProductRequest,
    ): ResponseEntity<ProductResponse> = ResponseEntity.ok(productService.update(id, request))

    @PutMapping("/{id}/active")
    @Operation(
        summary = "Изменить признак активности товара",
        description = "Назначение: Обновляет признак активности идентифицированного ресурса товара.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Товар должен существовать. Операция изменяет только признак активности.\nРезультат: Возвращает обновленный ресурс товара.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс товара обновлен.", content = [Content(schema = Schema(implementation = ProductResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun setActive(
        @Parameter(description = "Идентифицирует ресурс товара. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @Parameter(description = "Определяет целевой признак активности. Формат: boolean.", example = "false")
        @RequestParam active: Boolean,
    ): ResponseEntity<ProductResponse> = ResponseEntity.ok(productService.setActive(id, active))

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Деактивировать товар (legacy)",
        description = "Назначение: Выполняет устаревшую операцию деактивации товара для обратной совместимости.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Товар должен существовать. Операция выполняет мягкую деактивацию и не удаляет ресурс физически.\nРезультат: Тело ответа отсутствует.",
        deprecated = true,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Ресурс товара деактивирован."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun deactivate(
        @Parameter(description = "Идентифицирует ресурс товара. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        productService.deactivate(id)
        return ResponseEntity.noContent().build()
    }
}
