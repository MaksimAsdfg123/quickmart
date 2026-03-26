package com.quickmart.dto.category

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Представляет полезную нагрузку запроса на создание или обновление категории.")
data class CategoryRequest(
    @field:NotBlank
    @field:Size(max = 100)
    @field:Schema(description = "Указывает наименование категории. Ограничения: максимальная длина — 100.", example = "Vegetables and Fruits")
    val name: String,
    @field:Size(max = 500)
    @field:Schema(description = "Указывает описание категории. Ограничения: максимальная длина — 500.", example = "Fresh vegetables, fruits, and greens")
    val description: String?,
    @field:Schema(description = "Указывает, доступна ли категория для отображения в публичном каталоге.", example = "true")
    val active: Boolean = true,
)
