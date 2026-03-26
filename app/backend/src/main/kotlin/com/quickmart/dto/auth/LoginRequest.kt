package com.quickmart.dto.auth

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Представляет полезную нагрузку запроса на аутентификацию.")
data class LoginRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(description = "Указывает адрес электронной почты пользователя. Формат: строка email по RFC 5322.", example = "anna@example.com")
    val email: String,
    @field:NotBlank
    @field:Schema(description = "Указывает пароль пользователя.", example = "password")
    val password: String,
)
