package com.quickmart.service

import com.quickmart.dto.admin.InventoryStockResponse
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.InventoryMapper
import com.quickmart.repository.InventoryStockRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InventoryService(
    private val inventoryStockRepository: InventoryStockRepository,
    private val productService: ProductService,
    private val inventoryMapper: InventoryMapper,
) {
    fun getAll(
        page: Int,
        size: Int,
    ): List<InventoryStockResponse> {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.ASC, "createdAt"))
        return inventoryStockRepository
            .findAll(pageable)
            .content
            .map(inventoryMapper::toResponse)
    }

    fun getAvailableQuantity(productId: UUID): Int =
        inventoryStockRepository.findByProductId(productId)?.availableQuantity
            ?: throw NotFoundException("Остаток по товару не найден")

    fun ensureAvailable(
        productId: UUID,
        quantity: Int,
    ) {
        val available = getAvailableQuantity(productId)
        if (available < quantity) {
            throw BusinessException("Недостаточно товара на складе")
        }
    }

    @Transactional
    fun decreaseStockWithLock(
        productId: UUID,
        quantity: Int,
    ) {
        val stock =
            inventoryStockRepository
                .findByProductIdForUpdate(productId)
                .orElseThrow { NotFoundException("Остаток по товару не найден") }

        if (stock.availableQuantity < quantity) {
            throw BusinessException("Недостаточно товара: ${stock.product.name}")
        }

        stock.availableQuantity -= quantity
        inventoryStockRepository.save(stock)
    }

    @Transactional
    fun increaseStockWithLock(
        productId: UUID,
        quantity: Int,
    ) {
        val stock =
            inventoryStockRepository
                .findByProductIdForUpdate(productId)
                .orElseThrow { NotFoundException("Остаток по товару не найден") }

        stock.availableQuantity += quantity
        inventoryStockRepository.save(stock)
    }

    @Transactional
    fun updateStock(
        productId: UUID,
        newQuantity: Int,
    ): InventoryStockResponse {
        val stock =
            inventoryStockRepository.findByProductId(productId)
                ?: run {
                    val product = productService.getEntityByIdOrThrow(productId)
                    inventoryStockRepository.save(
                        com.quickmart.domain.entity.InventoryStock().apply {
                            this.product = product
                            this.availableQuantity = newQuantity
                        },
                    )
                }

        stock.availableQuantity = newQuantity
        return inventoryMapper.toResponse(inventoryStockRepository.save(stock))
    }
}
