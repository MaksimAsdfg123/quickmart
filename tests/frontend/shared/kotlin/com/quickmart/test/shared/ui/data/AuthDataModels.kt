package com.quickmart.test.shared.ui.data

data class UserCredentials(
    val email: String,
    val password: String,
)

data class NewUserRegistration(
    val fullName: String,
    val email: String,
    val password: String,
)

