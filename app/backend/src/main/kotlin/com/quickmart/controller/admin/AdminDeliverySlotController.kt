package com.quickmart.controller.admin

import com.quickmart.config.BAD_REQUEST_RESPONSE_REF
import com.quickmart.config.BEARER_AUTH_SCHEME
import com.quickmart.config.FORBIDDEN_RESPONSE_REF
import com.quickmart.config.INTERNAL_SERVER_ERROR_RESPONSE_REF
import com.quickmart.config.NOT_FOUND_RESPONSE_REF
import com.quickmart.config.TAG_ADMIN_DELIVERY_SLOTS
import com.quickmart.config.UNAUTHORIZED_ACCESS_RESPONSE_REF
import com.quickmart.dto.admin.DeliverySlotRequest
import com.quickmart.dto.admin.DeliverySlotResponse
import com.quickmart.service.DeliverySlotService
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/delivery-slots")
@Tag(name = TAG_ADMIN_DELIVERY_SLOTS, description = "Операции административного управления слотами доставки")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
class AdminDeliverySlotController(
    private val deliverySlotService: DeliverySlotService,
) {
    @PostMapping
    @Operation(
        summary = "Создать слот доставки",
        description = "Назначение: Создает ресурс слота доставки.\n" +
            "Авторизация: Bearer JWT с ролью `ADMIN`.\n" +
            "Ограничения: Полезная нагрузка запроса должна удовлетворять ограничениям валидации. `startTime` должно быть раньше `endTime`. `slotDate` не может находиться в прошлом.\n" +
            "Результат: Возвращает созданный ресурс слота доставки.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Ресурс слота доставки создан.", content = [Content(schema = Schema(implementation = DeliverySlotResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun create(
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая атрибуты создаваемого слота доставки.")
        @Valid @RequestBody request: DeliverySlotRequest,
    ): ResponseEntity<DeliverySlotResponse> = ResponseEntity.status(HttpStatus.CREATED).body(deliverySlotService.create(request))

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить слот доставки",
        description = "Назначение: Обновляет ресурс слота доставки, идентифицированный переданным идентификатором.\n" +
            "Авторизация: Bearer JWT с ролью `ADMIN`.\n" +
            "Ограничения: Слот доставки должен существовать. Ограничения валидации, действующие при создании, сохраняются и при обновлении.\n" +
            "Результат: Возвращает обновленный ресурс слота доставки.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Ресурс слота доставки обновлен.", content = [Content(schema = Schema(implementation = DeliverySlotResponse::class))]),
            ApiResponse(responseCode = "400", ref = BAD_REQUEST_RESPONSE_REF),
            ApiResponse(responseCode = "401", ref = UNAUTHORIZED_ACCESS_RESPONSE_REF),
            ApiResponse(responseCode = "403", ref = FORBIDDEN_RESPONSE_REF),
            ApiResponse(responseCode = "404", ref = NOT_FOUND_RESPONSE_REF),
            ApiResponse(responseCode = "500", ref = INTERNAL_SERVER_ERROR_RESPONSE_REF),
        ],
    )
    fun update(
        @Parameter(description = "Идентифицирует ресурс слота доставки. Формат: UUID.", example = "70000000-0000-0000-0000-000000000001")
        @PathVariable id: UUID,
        @SwaggerRequestBody(required = true, description = "Полезная нагрузка, определяющая целевые атрибуты слота доставки.")
        @Valid @RequestBody request: DeliverySlotRequest,
    ): ResponseEntity<DeliverySlotResponse> = ResponseEntity.ok(deliverySlotService.update(id, request))
}
