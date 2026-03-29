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
            // 요청마다 이전 인증 정보를 확실히 클리어하여
            // 헤더가 없는 요청(직접 접근)이 익명 상태로 흐르도록 보장합니다.
            SecurityContextHolder.clearContext();

            doLogin(request);
        } catch (Exception e) {
            // 필터 내부 에러가 401로 변질되지 않도록 로깅 후 통과시킵니다.
            logger.error("Failed to set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private void doLogin(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);

        // 직접 접근 시 헤더가 없으므로 여기서 즉시 return
        // 이후 Security의 AnonymousAuthenticationFilter가 익명 객체를 생성합니다.
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(username)) {
            return;
        }

        String name = request.getHeader(HEADER_USER_NAME);
        String email = request.getHeader(HEADER_EMAIL);
        String roles = request.getHeader(HEADER_ROLES);

        if (StringUtils.hasText(name)) {
            try {
                name = URLDecoder.decode(name, StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.warn("Failed to decode user name: " + name);
            }
        }

        try {
            // UUID 파싱 전 trim()으로 불필요한 공백 제거
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

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException e) {
            // 잘못된 UUID 형식이 들어와도 401을 던지지 않고 익명 상태로 진행시킵니다.
            logger.warn("Invalid UUID format from Gateway header: " + userId);
        }
    }
}