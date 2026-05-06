package org.example.board.provider

import io.jsonwebtoken.*
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
    // 💡 1. yml에서 두 개의 키를 각각 주입받습니다.
    @Value("\${jwt.access-secret}") private val accessSecretString: String,
    @Value("\${jwt.refresh-secret}") private val refreshSecretString: String
) {
    private lateinit var accessKey: Key
    private lateinit var refreshKey: Key

    private val accessTokenValidityInMilliseconds: Long = 1000 * 60 * 30 // 30분
    private val refreshTokenValidityInMilliseconds: Long = 1000 * 60 * 60 * 24 * 7 // 7일

    @PostConstruct
    fun init() {
        // 💡 2. 주입받은 문자열로 각각의 Key 객체를 생성합니다.
        val decodedAccessKey = Base64.getDecoder().decode(accessSecretString)
        this.accessKey = Keys.hmacShaKeyFor(decodedAccessKey)

        val decodedRefreshKey = Base64.getDecoder().decode(refreshSecretString)
        this.refreshKey = Keys.hmacShaKeyFor(decodedRefreshKey)
    }

    // Access Token 생성 (accessKey 사용)
    fun generateAccessToken(email: String, role: String): String {
        val now = Date()
        val validity = Date(now.time + accessTokenValidityInMilliseconds)

        return Jwts.builder()
            .setSubject(email)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(accessKey, SignatureAlgorithm.HS256) // 💡 accessKey로 서명
            .compact()
    }

    // Refresh Token 생성 (refreshKey 사용)
    fun generateRefreshToken(email: String): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidityInMilliseconds)

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(refreshKey, SignatureAlgorithm.HS256) // 💡 refreshKey로 서명
            .compact()
    }

    // 💡 3. 인증 정보 추출 및 토큰 검증은 주로 'Access Token'을 대상으로 이루어집니다.
    fun getAuthentication(accessToken: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(accessKey) // Access Token용 키로 복호화
            .build()
            .parseClaimsJws(accessToken)
            .body

        val authorities = listOf(SimpleGrantedAuthority(claims["role"].toString()))
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, accessToken, authorities)
    }

    fun validateAccessToken(accessToken: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(accessToken)
            true
        } catch (e: Exception) {
            false
        }
    }

    // (선택) Refresh Token이 유효한지 검사하는 전용 메서드 (나중에 토큰 재발급 API에서 사용)
    fun validateRefreshToken(refreshToken: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(refreshToken)
            true
        } catch (e: Exception) {
            false
        }
    }
}