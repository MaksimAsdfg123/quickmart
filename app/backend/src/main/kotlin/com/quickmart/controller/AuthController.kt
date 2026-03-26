package com.quickmart.controller

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.CONFLICT_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.TAG_AUTH
import com.quickmart.config.UNAUTHORIZED_LOGIN_RESPONSE_REF
import com.quickmart.dto.auth.AuthResponse
import com.quickmart.dto.auth.LoginRequest
import com.quickmart.dto.auth.RegisterRequest
import com.quickmart.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = TAG_AUTH, description = "Операции аутентификации и выдачи токенов")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @Operation(
        summary = "Зарегистрировать пользователя",
        description = "Назначение: Создает новую учетную запись пользователя и выдает JWT-токен доступа.\nАвторизация: Не требуется.\nОграничения: Адрес электронной почты должен быть уникальным. Пароль и полное имя должны удовлетворять ограничениям валидации запроса.\nРезультат: Возвращает выданный JWT и краткое описание аутентифицированного субъекта.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Учетная запись пользователя создана.", content = [Content(schema = Schema(implementation = AuthResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun register(
        @SwaggerRequestBody(
            required = true,
            description = "Полезная нагрузка, определяющая адрес электронной почты, пароль и полное имя создаваемого пользователя.",
        )
        @Valid @RequestBody request: RegisterRequest,
    ): ResponseEntity<AuthResponse> = ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    @PostMapping("/login")
    @Operation(
        summary = "Аутентифицировать субъекта",
        description = "Назначение: Выполняет аутентификацию существующего субъекта и выдает JWT-токен доступа.\nАвторизация: Не требуется.\nОграничения: Переданные учетные данные должны соответствовать активной учетной записи пользователя.\nРезультат: Возвращает выданный JWT и краткое описание аутентифицированного субъекта.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Аутентификация выполнена успешно.", content = [Content(schema = Schema(implementation = AuthResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_LOGIN_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun login(
        @SwaggerRequestBody(
            required = true,
            description = "Полезная нагрузка, определяющая адрес электронной почты и пароль субъекта, подлежащего аутентификации.",
        )
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthResponse> = ResponseEntity.ok(authService.login(request))
}

