package com.quickmart.controller

import com.quickmart.dto.category.CategoryResponse
import com.quickmart.service.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    fun getCategories(): ResponseEntity<List<CategoryResponse>> = ResponseEntity.ok(categoryService.getPublicCategories())
}
