package com.quickmart.service

import com.quickmart.domain.entity.Product
import com.quickmart.dto.PageResponse
import com.quickmart.dto.product.ProductRequest
import com.quickmart.dto.product.ProductResponse
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.ProductMapper
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.repository.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryService: CategoryService,
    private val inventoryStockRepository: InventoryStockRepository,
    private val productMapper: ProductMapper,
) {
    fun getCatalog(
        categoryId: UUID?,
        query: String?,
        page: Int,
        size: Int,
    ): PageResponse<ProductResponse> {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.ASC, "name"))
        val searchPattern =
            query
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?.let { "%$it%" }
        val products = productRepository.findCatalogPage(categoryId, searchPattern, pageable)
        return PageResponse.from(products, productMapper::toResponse)
    }

    fun getProduct(id: UUID): ProductResponse {
        val product =
            productRepository
                .findById(id)
                .orElseThrow { NotFoundException("Товар не найден") }
        return productMapper.toResponse(product)
    }

    fun getAllForAdmin(
        page: Int,
        size: Int,
    ): PageResponse<ProductResponse> {
        val normalizedPage = page.coerceAtLeast(0)
        val normalizedSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val products = productRepository.findAll(pageable)
        return PageResponse.from(products, productMapper::toResponse)
    }

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        val category = categoryService.getByIdOrThrow(request.categoryId)

        val product =
            Product().apply {
                name = request.name.trim()
                description = request.description?.trim()
                price = request.price
                this.category = category
                imageUrl = request.imageUrl?.trim()
                active = request.active
            }

        val saved = productRepository.save(product)
        val stock =
            com.quickmart.domain.entity.InventoryStock().apply {
                this.product = saved
                availableQuantity = 0
            }
        inventoryStockRepository.save(stock)
        return productMapper.toResponse(saved)
    }

    @Transactional
    fun update(
        id: UUID,
        request: ProductRequest,
    ): ProductResponse {
        val product =
            productRepository
                .findById(id)
                .orElseThrow { NotFoundException("Товар не найден") }
        val category = categoryService.getByIdOrThrow(request.categoryId)

        product.name = request.name.trim()
        product.description = request.description?.trim()
        product.price = request.price
        product.category = category
        product.imageUrl = request.imageUrl?.trim()
        product.active = request.active

        return productMapper.toResponse(productRepository.save(product))
    }

    @Transactional
    fun setActive(
        id: UUID,
        active: Boolean,
    ): ProductResponse {
        val product =
            productRepository
                .findById(id)
                .orElseThrow { NotFoundException("Товар не найден") }
        product.active = active
        return productMapper.toResponse(productRepository.save(product))
    }

    @Transactional
    fun deactivate(id: UUID) {
        val product =
            productRepository
                .findById(id)
                .orElseThrow { NotFoundException("Товар не найден") }
        product.active = false
        productRepository.save(product)
    }

    fun getEntityByIdOrThrow(id: UUID): Product =
        productRepository
            .findById(id)
            .orElseThrow { NotFoundException("Товар не найден") }
}
