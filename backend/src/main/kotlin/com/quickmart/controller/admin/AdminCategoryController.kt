package com.quickmart.controller.admin

import com.quickmart.dto.category.CategoryRequest
import com.quickmart.dto.category.CategoryResponse
import com.quickmart.service.CategoryService
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
class AdminCategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping
    fun all(): ResponseEntity<List<CategoryResponse>> = ResponseEntity.ok(categoryService.getAllCategories())

    @PostMapping
    fun create(
        @Valid @RequestBody request: CategoryRequest,
    ): ResponseEntity<CategoryResponse> = ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CategoryRequest,
    ): ResponseEntity<CategoryResponse> = ResponseEntity.ok(categoryService.update(id, request))

    @PutMapping("/{id}/active")
    fun setActive(
        @PathVariable id: UUID,
        @RequestParam active: Boolean,
    ): ResponseEntity<CategoryResponse> = ResponseEntity.ok(categoryService.setActive(id, active))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        categoryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
