package com.quickmart.test.shared.ui.api

data class AuthApiUser(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
)

data class AuthApiResponse(
    val token: String,
    val user: AuthApiUser,
)

data class AuthApiError(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: Map<String, String>? = null,
)

data class RegisterApiPayload(
    val email: String,
    val password: String,
    val fullName: String,
)

data class LoginApiPayload(
    val email: String,
    val password: String,
)

