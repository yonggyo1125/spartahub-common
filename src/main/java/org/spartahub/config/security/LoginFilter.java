package org.spartahub.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * API 게이트웨이를 통해 넘어온 회원 데이터를 로그인 처리
 *  X-User-Id : 회원 식별자
 *  X-Username : 로그인 아이디
 *  X-User-Roles
 *  X-User-Email
 *  X-User-Name : 회원명
 */
@Component
public class LoginFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_NAME = "X-User-Name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 핵심 보완: 로그인이 필요한 상황이 아니더라도,
            // 새로운 요청마다 이전의 인증 정보를 클리어하여 익명 상태를 보장합니다.
            SecurityContextHolder.clearContext();

            doLogin(request);
        } catch (Exception e) {
            // 필터 내부 에러가 401/500으로 번지지 않도록 로그만 남깁니다.
            logger.error("Failed to set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private void doLogin(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);

        // 1. 게이트웨이 헤더가 없으면 인증 처리를 하지 않고 즉시 종료 (익명 사용자 유지)
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(username)) {
            return;
        }

        String name = request.getHeader(HEADER_USER_NAME);
        String email = request.getHeader(HEADER_EMAIL);
        String roles = request.getHeader(HEADER_ROLES);

        // URL 디코딩 안전성 확보
        if (StringUtils.hasText(name)) {
            try {
                name = URLDecoder.decode(name, StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.warn("Failed to decode user name: " + name);
            }
        }

        try {
            // UUID 변환 전 공백 제거 등 기초적인 방어 코드
            String cleanUserId = userId.trim();

            UserDetails userDetails = UserDetailsImpl.builder()
                    .uuid(UUID.fromString(cleanUserId))
                    .username(username)
                    .email(email)
                    .name(name)
                    .roles(roles)
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException e) {
            // UUID 형식이 틀려도 401을 던지지 않고 익명 상태로 진행하게 둡니다.
            logger.warn("Invalid UUID format from Gateway header: " + userId);
        }
    }
}