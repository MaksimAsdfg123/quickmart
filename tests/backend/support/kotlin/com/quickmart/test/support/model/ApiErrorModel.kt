package com.quickmart.test.support.model

data class ApiErrorModel(
    val timestamp: String?,
    val status: Int,
    val error: String,
    val message: String,
    val path: String?,
    val fieldErrors: Map<String, String>?,
)

