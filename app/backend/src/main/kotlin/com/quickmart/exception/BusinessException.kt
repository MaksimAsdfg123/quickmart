package com.quickmart.exception

open class BusinessException(
    message: String,
    val status: Int = 400,
) : RuntimeException(message)
