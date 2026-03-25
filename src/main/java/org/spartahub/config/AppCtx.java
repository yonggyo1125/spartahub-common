package org.spartahub.config;

import org.spartahub.config.security.SecurityConfig;
import org.spartahub.config.security.SecurityConfigImpl;
import org.spartahub.config.swagger.SwaggerConfig;
import org.spartahub.config.swagger.SwaggerConfigImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

// 스프링 부트의 자동 설정 매커니즘에 참여하여 라이브러리 로드 시 자동 실행됨
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET) // 일반적인 서블릿 기반 웹 애플리케이션 환경일 때만 이 설정을 활성화함
public class AppCtx {

    // SecurityConfig로 등록된 빈이 없다면 등록
    @ConditionalOnMissingBean(SecurityConfig.class)
    public SecurityConfig securityConfig() {
        return new SecurityConfigImpl();
    }

    // SwaggerConfig로 등록된 빈이 없다면 등록
    @ConditionalOnMissingBean
    public SwaggerConfig swaggerConfig() {
        return new SwaggerConfigImpl();
    }


}
