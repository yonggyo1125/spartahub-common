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
            doLogin(request);
        } catch (Exception e) {
            logger.error("Failed to set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private void doLogin(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);

        // 필수 값이 없으면 인증 정보를 설정하지 않고 반환
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(username)) {
            return;
        }

        String name = request.getHeader(HEADER_USER_NAME);
        String email = request.getHeader(HEADER_EMAIL);
        String roles = request.getHeader(HEADER_ROLES);

        if (StringUtils.hasText(name)) {
            name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        }

        try {
            UserDetails userDetails = UserDetailsImpl.builder()
                    .uuid(UUID.fromString(userId))
                    .username(username)
                    .email(email)
                    .name(name)
                    .roles(roles)
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID format from Gateway header: " + userId);
        }
    }
}