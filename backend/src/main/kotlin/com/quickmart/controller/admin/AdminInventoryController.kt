package com.quickmart.controller.admin

import com.quickmart.dto.admin.InventoryStockResponse
import com.quickmart.dto.admin.InventoryUpdateRequest
import com.quickmart.service.InventoryService
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
class AdminInventoryController(
    private val inventoryService: InventoryService,
) {
    @GetMapping
    fun all(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) size: Int,
    ): ResponseEntity<List<InventoryStockResponse>> = ResponseEntity.ok(inventoryService.getAll(page, size))

    @PutMapping("/{productId}")
    fun update(
        @PathVariable productId: UUID,
        @Valid @RequestBody request: InventoryUpdateRequest,
    ): ResponseEntity<InventoryStockResponse> = ResponseEntity.ok(inventoryService.updateStock(productId, request.availableQuantity))
}
