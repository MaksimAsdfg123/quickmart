package com.quickmart.controller

import com.quickmart.dto.PageResponse
import com.quickmart.dto.product.ProductResponse
import com.quickmart.service.ProductService
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
class ProductController(
    private val productService: ProductService,
) {
    @GetMapping
    fun getCatalog(
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "12") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<PageResponse<ProductResponse>> = ResponseEntity.ok(productService.getCatalog(categoryId, q, page, size))

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: UUID,
    ): ResponseEntity<ProductResponse> = ResponseEntity.ok(productService.getProduct(id))
}
