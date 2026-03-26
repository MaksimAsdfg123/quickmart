package com.quickmart.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenService(
    private val jwtProperties: JwtProperties,
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateToken(principal: AppUserPrincipal): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(jwtProperties.expirationSeconds)

        return Jwts
            .builder()
            .subject(principal.username)
            .claim("uid", principal.id.toString())
            .claim("role", principal.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact()
    }

    fun extractClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun extractUsername(token: String): String = extractClaims(token).subject

    fun isTokenValid(token: String): Boolean {
        val claims = extractClaims(token)
        return claims.expiration.after(Date())
    }
}
