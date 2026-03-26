package com.quickmart.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.exception.ApiErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val body =
            ApiErrorResponse(
                status = 401,
                error = "Unauthorized",
                message = "Требуется авторизация",
                path = request.requestURI,
            )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
