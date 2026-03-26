package com.quickmart.controller

import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.TAG_CATEGORIES
import com.quickmart.dto.category.CategoryResponse
import com.quickmart.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
@Tag(name = TAG_CATEGORIES, description = "Операции публичного списка категорий")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список категорий",
        description = "Назначение: Возвращает коллекцию активных публичных категорий.\nАвторизация: Не требуется.\nОграничения: Возвращаются только активные категории.\nРезультат: Возвращает коллекцию категорий, отсортированную по наименованию категории.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Коллекция категорий возвращена."),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun getCategories(): ResponseEntity<List<CategoryResponse>> = ResponseEntity.ok(categoryService.getPublicCategories())
}
