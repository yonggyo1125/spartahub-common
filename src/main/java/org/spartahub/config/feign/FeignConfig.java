package org.spartahub.config.feign;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Configuration
@EnableFeignClients("org.spartahub")
public class FeignConfig {
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_NAME = "X-User-Name";


    @Bean
    public RequestInterceptor requestInterceptor() {
        return tpl -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest req = attrs.getRequest();

            // Authorization 헤더 전파
            String auth = req.getHeader("Authorization");
            if (StringUtils.hasText(auth)) tpl.header("Authorization", auth);

            // Gateway로부터 넘어온 사용자 정보 헤더들 전파
            List<String> userHeaders = List.of(
                    HEADER_TRACE_ID, HEADER_USER_ID, HEADER_USERNAME, HEADER_USER_NAME, HEADER_EMAIL, HEADER_ROLES
            );

            for (String headerName : userHeaders) {
                String value = req.getHeader(headerName);
                if (StringUtils.hasText(value)) {
                    tpl.header(headerName, value);
                }
            }
        };
    }
}
