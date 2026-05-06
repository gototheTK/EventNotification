package org.example.board.security

import org.assertj.core.api.Assertions.assertThat
import org.example.board.provider.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        // 테스트용으로 Base64 인코딩된 임의의 시크릿 키를 주입합니다.
        val testAccessSecret = "c2VjcmV0LWtleS1mb3ItYWNjZXNzLXRva2VuLXRlc3QtcHVycG9zZS1vbmx5"
        val testRefreshSecret = "c2VjcmV0LWtleS1mb3ItcmVmcmVzaC10b2tlbi10ZXN0LXB1cnBvc2Utb25seQ=="

        jwtTokenProvider = JwtTokenProvider(testAccessSecret, testRefreshSecret)
        jwtTokenProvider.init() // @PostConstruct 수동 실행
    }

    @Test
    @DisplayName("Access Token이 정상적으로 발급되고 검증을 통과해야 한다")
    fun generateAndValidateAccessToken() {
        // given
        val email = "test@example.com"
        val role = "ROLE_USER"

        // when
        val token = jwtTokenProvider.generateAccessToken(email, role)

        // then
        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateAccessToken(token)).isTrue()
    }

    @Test
    @DisplayName("Refresh Token이 정상적으로 발급되고 검증을 통과해야 한다")
    fun generateAndValidateRefreshToken() {
        // given
        val email = "test@example.com"

        // when
        val token = jwtTokenProvider.generateRefreshToken(email)

        // then
        assertThat(token).isNotBlank()
        assertThat(jwtTokenProvider.validateRefreshToken(token)).isTrue()
    }

    @Test
    @DisplayName("위조된 토큰은 검증에 실패해야 한다")
    fun validateFakeToken() {
        // given
        val fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload.signature"

        // when
        val isValid = jwtTokenProvider.validateAccessToken(fakeToken)

        // then
        assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("Access Token에서 정상적으로 인증 정보(Authentication)를 추출해야 한다")
    fun getAuthenticationFromToken() {
        // given
        val email = "admin@example.com"
        val role = "ROLE_ADMIN"
        val token = jwtTokenProvider.generateAccessToken(email, role)

        // when
        val authentication = jwtTokenProvider.getAuthentication(token)

        // then
        assertThat(authentication.name).isEqualTo(email)
        assertThat(authentication.authorities.first().authority).isEqualTo(role)
    }
}