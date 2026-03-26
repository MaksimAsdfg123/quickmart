package com.quickmart.security

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AuthFacade {
    fun userId(authentication: Authentication): UUID = (authentication.principal as AppUserPrincipal).id

    fun principal(authentication: Authentication): AppUserPrincipal = authentication.principal as AppUserPrincipal
}
