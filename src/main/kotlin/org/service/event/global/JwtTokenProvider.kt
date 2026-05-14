package org.service.event.global

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.access-secret}") private val accessSecretString: String,
    @Value("\${jwt.refresh-secret}") private val refreshSecretString: String
) {
    private lateinit var accessKey: Key
    private lateinit var refreshKey: Key

    private val accessTokenValidityInMilliseconds: Long = 1000 * 60 * 30
    private val refreshTokenValidityInMilliseconds: Long = 1000 * 60 * 60 * 24 * 7

    @PostConstruct
    fun init() {
        accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecretString))
        refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecretString))
    }

    fun generateAccessToken(email: String, role: String): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(email)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + accessTokenValidityInMilliseconds))
            .signWith(accessKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(email: String): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + refreshTokenValidityInMilliseconds))
            .signWith(refreshKey, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * JWT에서 인증 정보를 복원합니다.
     * subject에는 반드시 이메일을 저장해야 합니다(generateAccessToken 참고).
     * Spring Security User.username = email 이 되며, 이후 MemberRepository.findByEmail()로
     * 회원을 조회하는 모든 코드가 이 계약에 의존합니다.
     */
    fun getAuthentication(accessToken: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(accessKey)
            .build()
            .parseClaimsJws(accessToken)
            .body

        val email = claims.subject  // subject = email (generateAccessToken 계약)
        val role = claims["role"]?.toString()
            ?: throw IllegalArgumentException("JWT에 'role' 클레임이 없습니다.")
        val authorities = listOf(SimpleGrantedAuthority(role))
        val principal = User(email, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, accessToken, authorities)
    }

    fun validateAccessToken(accessToken: String): Boolean = runCatching {
        Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(accessToken)
    }.isSuccess

    fun validateRefreshToken(refreshToken: String): Boolean = runCatching {
        Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(refreshToken)
    }.isSuccess
}
