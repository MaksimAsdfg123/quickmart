package com.quickmart.test.support.model

import java.util.UUID

data class RegisterRequestModel(
    val email: String,
    val password: String,
    val fullName: String,
)

data class LoginRequestModel(
    val email: String,
    val password: String,
)

data class AuthResponseModel(
    val token: String,
    val user: AuthUserModel,
)

data class AuthUserModel(
    val id: UUID,
    val email: String,
    val fullName: String,
    val role: String,
)

