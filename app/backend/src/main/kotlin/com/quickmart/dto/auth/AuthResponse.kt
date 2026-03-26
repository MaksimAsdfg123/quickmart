package com.quickmart.dto.auth

import com.quickmart.domain.enums.Role
import java.util.UUID

data class AuthResponse(
    val token: String,
    val user: AuthUserDto,
)

data class AuthUserDto(
    val id: UUID,
    val email: String,
    val fullName: String,
    val role: Role,
)
