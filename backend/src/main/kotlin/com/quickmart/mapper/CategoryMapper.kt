package com.quickmart.mapper

import com.quickmart.domain.entity.Category
import com.quickmart.dto.category.CategoryResponse
import org.springframework.stereotype.Component

@Component
class CategoryMapper {
    fun toResponse(category: Category): CategoryResponse =
        CategoryResponse(
            id = category.id!!,
            name = category.name,
            description = category.description,
            active = category.active,
        )
}
