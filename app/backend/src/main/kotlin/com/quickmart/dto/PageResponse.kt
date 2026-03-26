package com.quickmart.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "Представляет стандартный пагинированный ответ.")
data class PageResponse<T>(
    @field:Schema(description = "Содержит элементы, возвращенные для текущей страницы.")
    val content: List<T>,
    @field:Schema(description = "Идентифицирует индекс страницы, нумерация начинается с нуля.", example = "0")
    val page: Int,
    @field:Schema(description = "Определяет запрошенный размер страницы.", example = "12")
    val size: Int,
    @field:Schema(description = "Содержит общее количество элементов, доступных для запроса.", example = "40")
    val totalElements: Long,
    @field:Schema(description = "Содержит общее количество доступных страниц.", example = "4")
    val totalPages: Int,
) {
    companion object {
        fun <T, R> from(
            page: Page<T>,
            mapper: (T) -> R,
        ): PageResponse<R> =
            PageResponse(
                content = page.content.map(mapper),
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
            )
    }
}
