package com.quickmart.controller.admin

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.CONFLICT_RESPONSE_REF
import com.quickmart.config.FORBIDDEN_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADMIN_PROMOS
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.promo.PromoCodeRequest
import com.quickmart.dto.promo.PromoCodeResponse
import com.quickmart.service.PromoCodeService
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/promocodes")
@Tag(name = TAG_ADMIN_PROMOS, description = "Операции административного управления промокодами")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AdminPromoCodeController(
    private val promoCodeService: PromoCodeService,
) {
    @GetMapping
    @Operation(
        summary = "Получить список промокодов",
        description = "Назначение: Возвращает полную коллекцию промокодов для административной обработки.\n" +
            "Авторизация: Bearer JWT с ролью `ADMIN`.\n" +
            "Ограничения: В выборку включаются активные, неактивные, отложенные, истекшие и исчерпанные промокоды. Порядок определяется реализацией сервиса.\n" +
            "Результат: Возвращает коллекцию промокодов.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Коллекция промокодов возвращена."),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun all(): ResponseEntity<List<PromoCodeResponse>> = ResponseEntity.ok(promoCodeService.getAll())

    @PostMapping
    @Operation(
        summary = "Создать промокод",
        description = "Назначение: Создает ресурс промокода.\n" +
            "Авторизация: Bearer JWT с ролью `ADMIN`.\n" +
            "Ограничения: Идентификатор промокода должен быть уникальным. Для `PERCENT` значение должно находиться в диапазоне `(0, 100]`. Для `FIXED` значение должно быть больше нуля. `validFrom` не может быть позже `validTo`. `usageLimit`, если передан, должен быть больше нуля.\n" +
            "Результат: Возвращает созданный ресурс промокода.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ресурс промокода создан.", content = [Content(schema = Schema(implementation = PromoCodeResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun create(
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая атрибуты создаваемого промокода.")
        @Valid @RequestBody request: PromoCodeRequest,
    ): ResponseEntity<PromoCodeResponse> = ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.create(request))

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить промокод",
        description = "Назначение: Обновляет ресурс промокода, идентифицированный переданным идентификатором.\n" +
            "Авторизация: Bearer JWT с ролью `ADMIN`.\n" +
            "Ограничения: Промокод должен существовать. Ограничения уникальности и валидации, действующие при создании, сохраняются и при обновлении.\n" +
            "Результат: Возвращает обновленный ресурс промокода.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс промокода обновлен.", content = [Content(schema = Schema(implementation = PromoCodeResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "409", ref = CONFLICT_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun update(
        @Parameter(description = "Идентифицирует ресурс промокода. Формат: UUID.", example = "60000000-0000-0000-0000-000000000002")
        @PathVariable id: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевые атрибуты промокода.")
        @Valid @RequestBody request: PromoCodeRequest,
    ): ResponseEntity<PromoCodeResponse> = ResponseEntity.ok(promoCodeService.update(id, request))

    @PutMapping("/{id}/active")
    @Operation(
        summary = "Изменить признак активности промокода",
        description = "Назначение: Обновляет признак активности идентифицированного ресурса промокода.\n" +
            "Авторизация: Bearer JWT с ролью `ADMIN`.\n" +
            "Ограничения: Промокод должен существовать. Операция изменяет только признак активности и не модифицирует конфигурацию скидки или счетчики использования.\n" +
            "Результат: Возвращает обновленный ресурс промокода.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс промокода обновлен.", content = [Content(schema = Schema(implementation = PromoCodeResponse::class))]),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun toggle(
        @Parameter(description = "Идентифицирует ресурс промокода. Формат: UUID.", example = "60000000-0000-0000-0000-000000000002")
        @PathVariable id: UUID,
        @Parameter(description = "Определяет целевой признак активности. Формат: boolean.", example = "false")
        @RequestParam active: Boolean,
    ): ResponseEntity<PromoCodeResponse> = ResponseEntity.ok(promoCodeService.toggle(id, active))
}
