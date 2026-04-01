package com.quickmart.service

import com.quickmart.cache.CacheNames
import com.quickmart.cache.CatalogReadCacheInvalidationPublisher
import com.quickmart.domain.entity.Product
import com.quickmart.dto.PageResponse
import com.quickmart.dto.product.ProductRequest
import com.quickmart.dto.product.ProductResponse
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.ProductMapper
import com.quickmart.repository.InventoryStockRepository
import com.quickmart.repository.ProductRepository
import org.springframework.cache.annotation.Cacheable
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
    private val catalogReadCacheInvalidationPublisher: CatalogReadCacheInvalidationPublisher,
) {
    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = [CacheNames.PUBLIC_CATALOG_PAGES],
        key = "@cacheKeyFactory.publicCatalog(#categoryId, #query, #page, #size)",
        sync = true,
    )
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

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = [CacheNames.PUBLIC_PRODUCT_CARDS],
        key = "@cacheKeyFactory.publicProduct(#id)",
        sync = true,
    )
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
        catalogReadCacheInvalidationPublisher.productChanged(saved.id!!)
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

        val saved = productRepository.save(product)
        catalogReadCacheInvalidationPublisher.productChanged(saved.id!!)
        return productMapper.toResponse(saved)
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
        val saved = productRepository.save(product)
        catalogReadCacheInvalidationPublisher.productChanged(saved.id!!)
        return productMapper.toResponse(saved)
    }

    @Transactional
    fun deactivate(id: UUID) {
        val product =
            productRepository
                .findById(id)
                .orElseThrow { NotFoundException("Товар не найден") }
        product.active = false
        val saved = productRepository.save(product)
        catalogReadCacheInvalidationPublisher.productChanged(saved.id!!)
    }

    fun getEntityByIdOrThrow(id: UUID): Product =
        productRepository
            .findById(id)
            .orElseThrow { NotFoundException("Товар не найден") }
}
