package com.quickmart.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(
    name = "ApiErrorResponse",
    description = "Представляет стандартный формат ошибки, возвращаемый API.",
)
data class ApiErrorResponse(
    @field:Schema(description = "Указывает временную отметку формирования ошибки на стороне сервера в формате ISO 8601.", example = "2026-03-26T10:15:30")
    val timestamp: LocalDateTime = LocalDateTime.now(),
    @field:Schema(description = "Указывает HTTP-код состояния, связанный с ошибкой.", example = "400")
    val status: Int,
    @field:Schema(description = "Содержит стандартную текстовую причину HTTP-статуса.", example = "Bad Request")
    val error: String,
    @field:Schema(description = "Содержит прикладное сообщение об ошибке.", example = "Сумма заказа меньше минимальной для промокода")
    val message: String,
    @field:Schema(description = "Содержит путь запроса, связанный с ошибкой.", example = "/api/orders/checkout", nullable = true)
    val path: String?,
    @field:Schema(description = "Содержит сообщения об ошибках валидации по полям при наличии ошибок валидации.", nullable = true)
    val fieldErrors: Map<String, String>? = null,
)
