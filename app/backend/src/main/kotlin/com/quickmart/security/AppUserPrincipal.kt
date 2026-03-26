package com.quickmart.security

import com.quickmart.domain.enums.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class AppUserPrincipal(
    val id: UUID,
    private val emailValue: String,
    private val passwordValue: String,
    val role: Role,
    private val active: Boolean,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = passwordValue

    override fun getUsername(): String = emailValue

    override fun isAccountNonExpired(): Boolean = active

    override fun isAccountNonLocked(): Boolean = active

    override fun isCredentialsNonExpired(): Boolean = active

    override fun isEnabled(): Boolean = active
}
