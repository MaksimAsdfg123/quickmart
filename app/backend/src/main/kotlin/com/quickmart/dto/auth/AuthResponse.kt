package com.quickmart.dto.auth

import com.quickmart.domain.enums.Role
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Представляет полезную нагрузку ответа аутентификации.")
data class AuthResponse(
    @field:Schema(description = "Содержит JWT-токен доступа.", example = "eyJhbGciOiJIUzI1NiJ9...")
    val token: String,
    @field:Schema(description = "Содержит краткое описание аутентифицированного субъекта.")
    val user: AuthUserDto,
)

@Schema(description = "Представляет краткое описание аутентифицированного субъекта.")
data class AuthUserDto(
    @field:Schema(description = "Идентифицирует пользователя. Формат: UUID.", example = "00000000-0000-0000-0000-000000000002")
    val id: UUID,
    @field:Schema(description = "Указывает адрес электронной почты пользователя.", example = "anna@example.com")
    val email: String,
    @field:Schema(description = "Указывает полное имя пользователя.", example = "Anna Petrova")
    val fullName: String,
    @field:Schema(description = "Определяет роль безопасности, назначенную аутентифицированному субъекту.", example = "CUSTOMER")
    val role: Role,
)
