package com.quickmart.service

import com.quickmart.domain.entity.Cart
import com.quickmart.domain.entity.User
import com.quickmart.domain.enums.Role
import com.quickmart.dto.auth.AuthResponse
import com.quickmart.dto.auth.AuthUserDto
import com.quickmart.dto.auth.LoginRequest
import com.quickmart.dto.auth.RegisterRequest
import com.quickmart.exception.BusinessException
import com.quickmart.exception.ConflictException
import com.quickmart.repository.CartRepository
import com.quickmart.repository.UserRepository
import com.quickmart.security.AppUserPrincipal
import com.quickmart.security.JwtTokenService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val fullName = request.fullName.trim()

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("Email уже зарегистрирован")
        }

        val user =
            User().apply {
                this.email = email
                passwordHash = passwordEncoder.encode(request.password)
                this.fullName = fullName
                role = Role.CUSTOMER
                active = true
            }

        val savedUser = userRepository.save(user)

        val cart =
            Cart().apply {
                this.user = savedUser
                active = true
            }
        cartRepository.save(cart)

        val principal =
            AppUserPrincipal(
                id = savedUser.id!!,
                emailValue = savedUser.email,
                passwordValue = savedUser.passwordHash,
                role = savedUser.role,
                active = savedUser.active,
            )
        val token = jwtTokenService.generateToken(principal)

        return AuthResponse(
            token = token,
            user =
                AuthUserDto(
                    id = savedUser.id!!,
                    email = savedUser.email,
                    fullName = savedUser.fullName,
                    role = savedUser.role,
                ),
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val authentication =
            try {
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(email, request.password),
                )
            } catch (ex: BadCredentialsException) {
                throw BusinessException("Неверный email или пароль", 401)
            }

        val principal = authentication.principal as AppUserPrincipal
        val token = jwtTokenService.generateToken(principal)

        return AuthResponse(
            token = token,
            user =
                AuthUserDto(
                    id = principal.id,
                    email = principal.username,
                    fullName = userRepository.findById(principal.id).orElseThrow().fullName,
                    role = principal.role,
                ),
        )
    }
}
