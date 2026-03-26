package com.quickmart.controller

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADDRESSES
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.address.AddressRequest
import com.quickmart.dto.address.AddressResponse
import com.quickmart.security.AuthFacade
import com.quickmart.service.AddressService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/addresses")
@Tag(name = TAG_ADDRESSES, description = "Операции с адресами пользователя")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AddressController(
    private val addressService: AddressService,
    private val authFacade: AuthFacade,
) {
    @GetMapping
    @Operation(
        summary = "Получить список адресов",
        description = "Назначение: Возвращает коллекцию адресов, принадлежащих аутентифицированному пользователю.\nАвторизация: Bearer JWT.\nОграничения: Возвращаются только адреса текущего субъекта.\nРезультат: Возвращает коллекцию адресов.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Коллекция адресов возвращена."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun getMyAddresses(
        @Parameter(hidden = true) authentication: Authentication,
    ): ResponseEntity<List<AddressResponse>> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(addressService.getMyAddresses(userId))
    }

    @PostMapping
    @Operation(
        summary = "Создать адрес",
        description = "Назначение: Создает ресурс адреса для аутентифицированного пользователя.\nАвторизация: Bearer JWT.\nОграничения: Поля запроса должны удовлетворять ограничениям валидации. Если `isDefault=true`, созданный адрес становится адресом по умолчанию.\nРезультат: Возвращает созданный ресурс адреса.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ресурс адреса создан.", content = [Content(schema = Schema(implementation = AddressResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun create(
        @Parameter(hidden = true) authentication: Authentication,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая атрибуты создаваемого адреса.")
        @Valid @RequestBody request: AddressRequest,
    ): ResponseEntity<AddressResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(userId, request))
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить адрес",
        description = "Назначение: Обновляет ресурс адреса, идентифицированный переданным идентификатором.\nАвторизация: Bearer JWT.\nОграничения: Адрес должен принадлежать аутентифицированному пользователю. Поля запроса должны удовлетворять ограничениям валидации.\nРезультат: Возвращает обновленный ресурс адреса.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс адреса обновлен.", content = [Content(schema = Schema(implementation = AddressResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun update(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует ресурс адреса. Формат: UUID.", example = "20000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевые атрибуты адреса.")
        @Valid @RequestBody request: AddressRequest,
    ): ResponseEntity<AddressResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(addressService.update(userId, id, request))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить адрес",
        description = "Назначение: Удаляет ресурс адреса, идентифицированный переданным идентификатором.\nАвторизация: Bearer JWT.\nОграничения: Адрес должен принадлежать аутентифицированному пользователю.\nРезультат: Тело ответа отсутствует.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Ресурс адреса удален."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun delete(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(description = "Идентифицирует ресурс адреса. Формат: UUID.", example = "20000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        val userId = authFacade.userId(authentication)
        addressService.delete(userId, id)
        return ResponseEntity.noContent().build()
    }
}


