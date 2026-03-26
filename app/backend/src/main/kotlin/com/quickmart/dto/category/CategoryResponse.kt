package com.quickmart.dto.category

import java.util.UUID

data class CategoryResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val active: Boolean,
)
