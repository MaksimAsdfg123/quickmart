package com.quickmart.dto.category

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Представляет ресурс категории.")
data class CategoryResponse(
    @field:Schema(description = "Идентифицирует категорию. Формат: UUID.", example = "30000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Содержит наименование категории.", example = "Vegetables and Fruits")
    val name: String,
    @field:Schema(description = "Содержит описание категории.", example = "Fresh vegetables, fruits, and greens")
    val description: String?,
    @field:Schema(description = "Указывает, активна ли категория.", example = "true")
    val active: Boolean,
)
