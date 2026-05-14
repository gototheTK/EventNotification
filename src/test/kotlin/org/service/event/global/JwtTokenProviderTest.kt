package org.service.event.global

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        val testAccessSecret  = "c2VjcmV0LWtleS1mb3ItYWNjZXNzLXRva2VuLXRlc3QtcHVycG9zZS1vbmx5"
        val testRefreshSecret = "c2VjcmV0LWtleS1mb3ItcmVmcmVzaC10b2tlbi10ZXN0LXB1cnBvc2Utb25seQ=="

        jwtTokenProvider = JwtTokenProvider(testAccessSecret, testRefreshSecret)
        jwtTokenProvider.init()
    }

    @Test
    @DisplayName("Access Token이 정상적으로 발급되고 검증을 통과해야 한다")
    fun generateAndValidateAccessToken() {
        val token = jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER")
        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue()
    }

    @Test
    @DisplayName("Refresh Token이 정상적으로 발급되고 검증을 통과해야 한다")
    fun generateAndValidateRefreshToken() {
        val token = jwtTokenProvider.generateRefreshToken("test@example.com")
        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateRefreshToken(token)).isTrue()
    }

    @Test
    @DisplayName("위조된 토큰은 검증에 실패해야 한다")
    fun validateFakeToken() {
        val isValid = jwtTokenProvider.validateAccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature")
        assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("Access Token에서 정상적으로 인증 정보(Authentication)를 추출해야 한다")
    fun getAuthenticationFromToken() {
        val token = jwtTokenProvider.generateAccessToken("admin@example.com", "ROLE_ADMIN")
        val authentication = jwtTokenProvider.getAuthentication(token)

        assertThat(authentication.name).isEqualTo("admin@example.com")
        assertThat(authentication.authorities.first().authority).isEqualTo("ROLE_ADMIN")
    }
}
