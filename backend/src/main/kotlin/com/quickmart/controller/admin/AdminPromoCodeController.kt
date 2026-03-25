package com.quickmart.controller.admin

import com.quickmart.dto.promo.PromoCodeRequest
import com.quickmart.dto.promo.PromoCodeResponse
import com.quickmart.service.PromoCodeService
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
class AdminPromoCodeController(
    private val promoCodeService: PromoCodeService,
) {
    @GetMapping
    fun all(): ResponseEntity<List<PromoCodeResponse>> = ResponseEntity.ok(promoCodeService.getAll())

    @PostMapping
    fun create(
        @Valid @RequestBody request: PromoCodeRequest,
    ): ResponseEntity<PromoCodeResponse> = ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PromoCodeRequest,
    ): ResponseEntity<PromoCodeResponse> = ResponseEntity.ok(promoCodeService.update(id, request))

    @PutMapping("/{id}/active")
    fun toggle(
        @PathVariable id: UUID,
        @RequestParam active: Boolean,
    ): ResponseEntity<PromoCodeResponse> = ResponseEntity.ok(promoCodeService.toggle(id, active))
}
