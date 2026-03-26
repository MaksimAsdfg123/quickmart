package com.quickmart.exception

class NotFoundException(
    message: String,
) : BusinessException(message, 404)
