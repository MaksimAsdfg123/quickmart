package com.quickmart.dto.auth

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Представляет полезную нагрузку запроса на регистрацию пользователя.")
data class RegisterRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(description = "Указывает уникальный адрес электронной почты нового пользователя. Формат: строка email по RFC 5322.", example = "new.customer@example.com")
    val email: String,
    @field:NotBlank
    @field:Size(min = 8, max = 100)
    @field:Schema(description = "Указывает пароль пользователя. Ограничения: минимальная длина — 8, максимальная — 100.", example = "password123")
    val password: String,
    @field:NotBlank
    @field:Size(max = 120)
    @field:Schema(description = "Указывает полное имя пользователя. Ограничения: максимальная длина — 120.", example = "Anna Petrova")
    val fullName: String,
)

