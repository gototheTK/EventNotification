package org.example.board.config

import org.example.board.filter.JwtAuthenticationFilter
import org.example.board.provider.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    // 누구나 접근 가능한 API (회원가입, 로그인, 서울시 API 수동 동기화 테스트 등)
                    .requestMatchers("/api/auth/**", "/api/test/**").permitAll()

                    // 행사 목록 조회 등은 로그인 없이도 볼 수 있게 허용 (GET 요청만)
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/events/**").permitAll()

                    // 그 외의 모든 요청(찜하기 등)은 무조건 인증된 사용자(JWT 토큰 보유자)만 접근 가능
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()

    }

}