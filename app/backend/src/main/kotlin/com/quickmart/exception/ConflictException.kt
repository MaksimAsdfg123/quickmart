package com.quickmart.exception

class ConflictException(
    message: String,
) : BusinessException(message, 409)
