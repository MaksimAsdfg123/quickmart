package com.quickmart.controller

import com.quickmart.dto.cart.AddCartItemRequest
import com.quickmart.dto.cart.CartResponse
import com.quickmart.dto.cart.UpdateCartItemRequest
import com.quickmart.security.AuthFacade
import com.quickmart.service.CartService
import jakarta.validation.Valid
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
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService,
    private val authFacade: AuthFacade,
) {
    @GetMapping
    fun getCart(authentication: Authentication): ResponseEntity<CartResponse> =
        ResponseEntity.ok(cartService.getCart(authFacade.userId(authentication)))

    @PostMapping("/items")
    fun addItem(
        authentication: Authentication,
        @Valid @RequestBody request: AddCartItemRequest,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.addItem(authFacade.userId(authentication), request))

    @PutMapping("/items/{productId}")
    fun updateItem(
        authentication: Authentication,
        @PathVariable productId: UUID,
        @Valid @RequestBody request: UpdateCartItemRequest,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.updateItem(authFacade.userId(authentication), productId, request))

    @DeleteMapping("/items/{productId}")
    fun removeItem(
        authentication: Authentication,
        @PathVariable productId: UUID,
    ): ResponseEntity<CartResponse> = ResponseEntity.ok(cartService.removeItem(authFacade.userId(authentication), productId))
}
