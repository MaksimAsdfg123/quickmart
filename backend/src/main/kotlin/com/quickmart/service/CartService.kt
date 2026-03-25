package com.quickmart.service

import com.quickmart.domain.entity.Cart
import com.quickmart.domain.entity.CartItem
import com.quickmart.dto.cart.AddCartItemRequest
import com.quickmart.dto.cart.CartResponse
import com.quickmart.dto.cart.UpdateCartItemRequest
import com.quickmart.exception.BusinessException
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.CartMapper
import com.quickmart.repository.CartRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val productService: ProductService,
    private val inventoryService: InventoryService,
    private val cartMapper: CartMapper,
) {
    @Transactional
    fun getCart(userId: UUID): CartResponse {
        val cart = getOrCreateCart(userId)
        return cartMapper.toResponse(cart)
    }

    @Transactional
    fun addItem(
        userId: UUID,
        request: AddCartItemRequest,
    ): CartResponse {
        val cart = getOrCreateCart(userId)
        val product = productService.getEntityByIdOrThrow(request.productId)
        if (!product.active) {
            throw BusinessException("Товар недоступен для заказа")
        }

        val existing = cart.items.firstOrNull { it.product.id == request.productId }
        val newQuantity = (existing?.quantity ?: 0) + request.quantity
        inventoryService.ensureAvailable(request.productId, newQuantity)

        if (existing != null) {
            existing.quantity = newQuantity
        } else {
            val item =
                CartItem().apply {
                    this.cart = cart
                    this.product = product
                    this.quantity = request.quantity
                }
            cart.items.add(item)
        }

        return cartMapper.toResponse(cartRepository.save(cart))
    }

    @Transactional
    fun updateItem(
        userId: UUID,
        productId: UUID,
        request: UpdateCartItemRequest,
    ): CartResponse {
        val cart = getOrCreateCart(userId)
        val item =
            cart.items.firstOrNull { it.product.id == productId }
                ?: throw NotFoundException("Товар не найден в корзине")

        if (request.quantity == 0) {
            cart.items.remove(item)
        } else {
            if (!item.product.active) {
                throw BusinessException("Товар недоступен для заказа")
            }
            inventoryService.ensureAvailable(productId, request.quantity)
            item.quantity = request.quantity
        }

        return cartMapper.toResponse(cartRepository.save(cart))
    }

    @Transactional
    fun removeItem(
        userId: UUID,
        productId: UUID,
    ): CartResponse {
        val cart = getOrCreateCart(userId)
        cart.items.removeIf { it.product.id == productId }
        return cartMapper.toResponse(cartRepository.save(cart))
    }

    @Transactional
    fun clear(userId: UUID) {
        val cart = getOrCreateCart(userId)
        cart.items.clear()
        cartRepository.save(cart)
    }

    @Transactional
    fun getOrCreateCart(userId: UUID): Cart {
        val existing = cartRepository.findByUserIdAndActiveTrue(userId)
        if (existing.isPresent) {
            return existing.get()
        }

        val user = userService.getByIdOrThrow(userId)
        val cart =
            Cart().apply {
                this.user = user
                active = true
            }
        return cartRepository.save(cart)
    }
}
