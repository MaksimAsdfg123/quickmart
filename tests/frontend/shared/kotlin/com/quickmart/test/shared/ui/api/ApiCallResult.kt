package com.quickmart.test.shared.ui.api

data class ApiCallResult<T>(
    val statusCode: Int,
    val body: String,
    val data: T? = null,
)

