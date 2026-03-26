package com.quickmart.service

import com.quickmart.domain.entity.Category
import com.quickmart.dto.category.CategoryRequest
import com.quickmart.dto.category.CategoryResponse
import com.quickmart.exception.ConflictException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.CategoryMapper
import com.quickmart.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryMapper: CategoryMapper,
) {
    fun getPublicCategories(): List<CategoryResponse> =
        categoryRepository
            .findAllByActiveTrueOrderByNameAsc()
            .map(categoryMapper::toResponse)

    fun getAllCategories(): List<CategoryResponse> =
        categoryRepository
            .findAll()
            .sortedBy { it.name.lowercase() }
            .map(categoryMapper::toResponse)

    @Transactional
    fun create(request: CategoryRequest): CategoryResponse {
        if (categoryRepository.existsByNameIgnoreCase(request.name)) {
            throw ConflictException("Категория с таким названием уже существует")
        }

        val category =
            Category().apply {
                name = request.name.trim()
                description = request.description?.trim()
                active = request.active
            }
        return categoryMapper.toResponse(categoryRepository.save(category))
    }

    @Transactional
    fun update(
        id: UUID,
        request: CategoryRequest,
    ): CategoryResponse {
        val category =
            categoryRepository
                .findById(id)
                .orElseThrow { NotFoundException("Категория не найдена") }

        if (category.name.lowercase() != request.name.lowercase() && categoryRepository.existsByNameIgnoreCase(request.name)) {
            throw ConflictException("Категория с таким названием уже существует")
        }

        category.name = request.name.trim()
        category.description = request.description?.trim()
        category.active = request.active
        return categoryMapper.toResponse(categoryRepository.save(category))
    }

    @Transactional
    fun setActive(
        id: UUID,
        active: Boolean,
    ): CategoryResponse {
        val category =
            categoryRepository
                .findById(id)
                .orElseThrow { NotFoundException("Категория не найдена") }
        category.active = active
        return categoryMapper.toResponse(categoryRepository.save(category))
    }

    @Transactional
    fun delete(id: UUID) {
        val category =
            categoryRepository
                .findById(id)
                .orElseThrow { NotFoundException("Категория не найдена") }
        category.active = false
        categoryRepository.save(category)
    }

    fun getByIdOrThrow(id: UUID): Category =
        categoryRepository
            .findById(id)
            .orElseThrow { NotFoundException("Категория не найдена") }
}
