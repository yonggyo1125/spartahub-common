package org.spartahub.common.response;

public record CommonResponse<T>(
        boolean success,
        String message,
        T data,
        String traceId // 로그 추적용 ID (MDC 연동)
) {
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "요청이 성공적으로 처리되었습니다.", data, org.slf4j.MDC.get("traceId"));
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data, org.slf4j.MDC.get("traceId"));
    }
}