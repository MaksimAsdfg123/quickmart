package com.quickmart.controller.admin

import com.quickmart.dto.PageResponse
import com.quickmart.dto.product.ProductRequest
import com.quickmart.dto.product.ProductResponse
import com.quickmart.service.ProductService
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
class AdminProductController(
    private val productService: ProductService,
) {
    @GetMapping
    fun all(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<PageResponse<ProductResponse>> = ResponseEntity.ok(productService.getAllForAdmin(page, size))

    @PostMapping
    fun create(
        @Valid @RequestBody request: ProductRequest,
    ): ResponseEntity<ProductResponse> = ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ProductRequest,
    ): ResponseEntity<ProductResponse> = ResponseEntity.ok(productService.update(id, request))

    @PutMapping("/{id}/active")
    fun setActive(
        @PathVariable id: UUID,
        @RequestParam active: Boolean,
    ): ResponseEntity<ProductResponse> = ResponseEntity.ok(productService.setActive(id, active))

    @DeleteMapping("/{id}")
    fun deactivate(
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        productService.deactivate(id)
        return ResponseEntity.noContent().build()
    }
}
