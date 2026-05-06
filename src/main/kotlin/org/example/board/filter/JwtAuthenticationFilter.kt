package org.example.board.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.board.provider.JwtTokenProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. HTTP 헤더에서 토큰 추출
        val token = resolveToken(request)

        // 2. 토큰이 존재하고 유효한지 검사
        if (token != null && jwtTokenProvider.validateAccessToken(token)) {
            // 3. 토큰이 유효하면 인증 정보를 꺼내서 SecurityContext에 저장 (이후 컨트롤러에서 꺼내 쓸 수 있음)
            val authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        // 4. 다음 필터로 이동
        filterChain.doFilter(request, response)
    }

    // HTTP 요청 헤더에서 "Bearer " 접두사를 제거하고 순수 토큰만 뽑아내는 도우미 메서드
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }
}