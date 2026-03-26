package com.quickmart.controller.admin

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.CONFLICT_RESPONSE_REF
import com.quickmart.config.FORBIDDEN_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADMIN_CATEGORIES
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.category.CategoryRequest
import com.quickmart.dto.category.CategoryResponse
import com.quickmart.service.CategoryService
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = TAG_ADMIN_CATEGORIES, description = "Операции административного управления категориями")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AdminCategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список категорий",
        description = "Назначение: Возвращает полную коллекцию категорий для административной обработки.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: В выборку включаются как активные, так и неактивные категории.\nРезультат: Возвращает коллекцию категорий.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Коллекция категорий возвращена."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun all(): ResponseEntity<List<CategoryResponse>> = ResponseEntity.ok(categoryService.getAllCategories())

    @PostMapping
    @Operation(
        summary = "Создать категорию",
        description = "Назначение: Создает ресурс категории.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Наименование категории должно быть уникальным. Полезная нагрузка запроса должна удовлетворять ограничениям валидации.\nРезультат: Возвращает созданный ресурс категории.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ресурс категории создан.", content = [Content(schema = Schema(implementation = CategoryResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun create(
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая атрибуты создаваемой категории.")
        @Valid @RequestBody request: CategoryRequest,
    ): ResponseEntity<CategoryResponse> = ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request))

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить категорию",
        description = "Назначение: Обновляет ресурс категории, идентифицированный переданным идентификатором.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Категория должна существовать. Наименование категории должно оставаться уникальным. Полезная нагрузка запроса должна удовлетворять ограничениям валидации.\nРезультат: Возвращает обновленный ресурс категории.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс категории обновлен.", content = [Content(schema = Schema(implementation = CategoryResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun update(
        @Parameter(description = "Идентифицирует ресурс категории. Формат: UUID.", example = "30000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевые атрибуты категории.")
        @Valid @RequestBody request: CategoryRequest,
    ): ResponseEntity<CategoryResponse> = ResponseEntity.ok(categoryService.update(id, request))

    @PutMapping("/{id}/active")
    @Operation(
        summary = "Изменить признак активности категории",
        description = "Назначение: Обновляет признак активности идентифицированного ресурса категории.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Категория должна существовать. Операция изменяет только признак активности.\nРезультат: Возвращает обновленный ресурс категории.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс категории обновлен.", content = [Content(schema = Schema(implementation = CategoryResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun setActive(
        @Parameter(description = "Идентифицирует ресурс категории. Формат: UUID.", example = "30000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @Parameter(description = "Определяет целевой признак активности. Формат: boolean.", example = "false")
        @RequestParam active: Boolean,
    ): ResponseEntity<CategoryResponse> = ResponseEntity.ok(categoryService.setActive(id, active))

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Деактивировать категорию (legacy)",
        description = "Назначение: Выполняет устаревшую операцию деактивации категории для обратной совместимости.\nАвторизация: Bearer JWT с ролью `ADMIN`.\nОграничения: Категория должна существовать. Операция выполняет мягкую деактивацию и не удаляет ресурс физически.\nРезультат: Тело ответа отсутствует.",
        deprecated = true,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Ресурс категории деактивирован."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun delete(
        @Parameter(description = "Идентифицирует ресурс категории. Формат: UUID.", example = "30000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        categoryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
