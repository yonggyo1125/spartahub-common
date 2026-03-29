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

                // 익명 사용자 설정을 명시적으로 활성화 (이게 없으면 permitAll도 401이 날 수 있음)
                .anonymous(anonymous -> anonymous
                        .principal("anonymousUser")
                        .authorities("ROLE_ANONYMOUS")
                )

                //  필터 순서: LoginFilter에서 실패해도 뒤로 넘어가게 설계됨
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorize -> authorize
                        // 테스트를 위해 모든 경로를 완전히 개방
                        .anyRequest().permitAll()
                )

                .exceptionHandling(c -> {
                    c.authenticationEntryPoint((req, res, e) -> {
                        log.error("Security EntryPoint 차단: {} - {}", req.getRequestURI(), e.getMessage());
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    });
                });

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> auditorProvider() {
        return SecurityUtil::getCurrentUsername;
    }
}