package org.spartahub.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spartahub.common.util.SecurityUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 도메인 로직(메서드 수준)에서 권한 통제를 하기 위해 유지
@RequiredArgsConstructor
public class SecurityConfigImpl implements SecurityConfig {

    private final LoginFilter loginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 1. 익명 사용자 활성화 (필수)
                .anonymous(anonymous -> anonymous.principal("anonymousUser"))

                // 2. 필터 순서 유지
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)

                // 3. 모든 경로 완전 허용 (인가 통제권을 도메인으로 위임)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )

                // 4. 예외 핸들링 보완
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint((req, res, e) -> {
                        // 어떤 원인으로 401이 발생하는지 로그로 확인
                        log.error("401 Unauthorized 발생 경로: {}, 사유: {}", req.getRequestURI(), e.getMessage());
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    });
                    c.accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN));
                });

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> auditorProvider() {
        return SecurityUtil::getCurrentUsername;
    }
}