package com.quickmart.security

import com.quickmart.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): AppUserPrincipal {
        val user =
            userRepository
                .findByEmail(username)
                .orElseThrow { UsernameNotFoundException("User not found") }

        return AppUserPrincipal(
            id = user.id!!,
            emailValue = user.email,
            passwordValue = user.passwordHash,
            role = user.role,
            active = user.active,
        )
    }
}
