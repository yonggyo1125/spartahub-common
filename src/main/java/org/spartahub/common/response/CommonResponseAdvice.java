package org.spartahub.common.response;

import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "org.spartahub")
public class CommonResponseAdvice implements ResponseBodyAdvice<Object>  {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // CommonResponse를 직접 쓰거나 String으로 반환하는 경우는 변환 배제
        return !returnType.getParameterType().equals(CommonResponse.class) &&
                !returnType.getParameterType().equals(String.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 컨트롤러가 반환한 데이터를 CommonResponse.success() 메서드로 감싸서 반환
        return new CommonResponse<>(
                true,
                "요청이 성공적으로 처리되었습니다.",
                body,
                MDC.get("traceId")
        );
    }
}
