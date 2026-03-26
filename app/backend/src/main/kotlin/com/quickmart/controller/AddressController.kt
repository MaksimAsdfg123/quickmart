package com.quickmart.controller

import com.quickmart.dto.address.AddressRequest
import com.quickmart.dto.address.AddressResponse
import com.quickmart.security.AuthFacade
import com.quickmart.service.AddressService
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
class AddressController(
    private val addressService: AddressService,
    private val authFacade: AuthFacade,
) {
    @GetMapping
    fun getMyAddresses(authentication: Authentication): ResponseEntity<List<AddressResponse>> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(addressService.getMyAddresses(userId))
    }

    @PostMapping
    fun create(
        authentication: Authentication,
        @Valid @RequestBody request: AddressRequest,
    ): ResponseEntity<AddressResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(userId, request))
    }

    @PutMapping("/{id}")
    fun update(
        authentication: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: AddressRequest,
    ): ResponseEntity<AddressResponse> {
        val userId = authFacade.userId(authentication)
        return ResponseEntity.ok(addressService.update(userId, id, request))
    }

    @DeleteMapping("/{id}")
    fun delete(
        authentication: Authentication,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        val userId = authFacade.userId(authentication)
        addressService.delete(userId, id)
        return ResponseEntity.noContent().build()
    }
}
