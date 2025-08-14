package org.example.websocketsamplespring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (JWT 사용 시 일반적으로 비활성화)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 세션 관리 정책을 STATELESS로 설정 (JWT 사용 시)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // "/ws/**" 경로는 인증 없이도 접근 허용 (웹소켓 핸드셰이크를 위함)
                        .requestMatchers("/ws/**").permitAll()
                        // 로그인, 회원가입 등 인증이 필요 없는 API 경로도 허용
                        .requestMatchers("/api/login", "/api/signup").permitAll()
                        // 그 외 모든 요청은 반드시 인증 필요
                        .anyRequest().authenticated()
                );

        // JWT를 사용한다면 여기에 JWT 인증 필터를 추가해야 합니다.
        // http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
