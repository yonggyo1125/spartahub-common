package org.spartahub.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 도메인 로직(메서드 수준)에서 권한 통제를 하기 위해 유지
@RequiredArgsConstructor
public class SecurityConfigImpl implements SecurityConfig {

    private final LoginFilter loginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 보안 설정 비활성화 (Stateless API)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. 익명 사용자 허용 (인증 헤더가 없어도 컨트롤러까지 도달하도록 보장)
                .anonymous(anonymous -> anonymous.principal("anonymousUser"))

                // 3. 커스텀 필터 배치
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)

                // 4. 모든 요청을 조건 없이 허용 (중복된 matcher 제거)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )

                // 5. 예외 핸들링 (도메인 로직 내 @PreAuthorize 실패 시 작동)
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
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