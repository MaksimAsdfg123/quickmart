package com.quickmart.controller

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_CATALOG
import com.quickmart.dto.PageResponse
import com.quickmart.dto.product.ProductResponse
import com.quickmart.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/products")
@Tag(name = TAG_CATALOG, description = "Операции публичного каталога товаров")
class ProductController(
    private val productService: ProductService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список товаров",
        description = "Назначение: Возвращает пагинированную коллекцию публично доступных товаров.\nАвторизация: Не требуется.\nОграничения: `page` использует нумерацию с нуля. `size` должен находиться в допустимом диапазоне валидации. Дополнительная фильтрация поддерживается параметрами `categoryId` и `q`.\nРезультат: Возвращает пагинированную коллекцию товаров, упорядоченную по наименованию товара.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Пагинированная коллекция товаров возвращена."),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun getCatalog(
        @Parameter(description = "Идентифицирует фильтр по категории. Формат: UUID. Параметр является необязательным.", example = "30000000-0000-0000-0000-000000000001")
        @RequestParam(required = false) categoryId: UUID?,
        @Parameter(description = "Указывает выражение поиска без учета регистра, применяемое к наименованию товара. Параметр является необязательным.", example = "banana")
        @RequestParam(required = false) q: String?,
        @Parameter(description = "Идентифицирует индекс страницы с нумерацией от нуля. Формат: целое число. Минимальное значение — 0.", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "Определяет размер страницы. Формат: целое число. Минимальное значение — 1. Максимальное значение — 200.", example = "12")
        @RequestParam(defaultValue = "12") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<PageResponse<ProductResponse>> = ResponseEntity.ok(productService.getCatalog(categoryId, q, page, size))

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить товар",
        description = "Назначение: Возвращает ресурс товара, идентифицированный переданным идентификатором.\nАвторизация: Не требуется.\nОграничения: Идентификатор должен ссылаться на существующий ресурс товара.\nРезультат: Возвращает ресурс товара.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс товара возвращен.", content = [Content(schema = Schema(implementation = ProductResponse::class))]),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun getProduct(
        @Parameter(description = "Идентифицирует ресурс товара. Формат: UUID.", example = "40000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<ProductResponse> = ResponseEntity.ok(productService.getProduct(id))
}
