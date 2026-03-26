package com.quickmart.domain.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Определяет роль безопасности, назначенную аутентифицированному субъекту.",
    example = "CUSTOMER",
)
enum class Role {
    CUSTOMER,
    ADMIN,
}
