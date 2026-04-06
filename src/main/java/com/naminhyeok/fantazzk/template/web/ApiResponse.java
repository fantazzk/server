package com.naminhyeok.fantazzk.template.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public final class ApiResponse<T> {
    @Schema(description = "응답 종류입니다.")
    private final ResultType resultType;

    @Schema(description = "성공 응답 payload 입니다. resultType 이 SUCCESS 일 때만 값이 있습니다.")
    private final T success;

    @Schema(description = "실패 응답 정보입니다. resultType 이 ERROR 일 때만 값이 있습니다.")
    private final ErrorResponse error;

    public ApiResponse(
        ResultType resultType,
        T success,
        ErrorResponse error
    ) {
        this.resultType = resultType;
        this.success = success;
        this.error = error;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public T getSuccess() {
        return success;
    }

    public ErrorResponse getError() {
        return error;
    }

    public enum ResultType {
        SUCCESS,
        ERROR,
    }

    public static final class ErrorResponse {
        @Schema(description = "HTTP status code 입니다.")
        private final int status;

        @Schema(description = "클라이언트가 분기 처리할 애플리케이션 에러 코드입니다.")
        private final String errorCode;

        @Schema(description = "사람이 읽을 수 있는 실패 사유입니다.")
        private final String reason;

        @Schema(description = "추가 오류 정보가 있을 때만 포함됩니다.")
        private final Map<String, Object> data;

        public ErrorResponse(
            int status,
            String errorCode,
            String reason,
            Map<String, Object> data
        ) {
            this.status = status;
            this.errorCode = errorCode;
            this.reason = reason;
            this.data = data;
        }

        public int getStatus() {
            return status;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getReason() {
            return reason;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<Object> error(
        int status,
        String errorCode,
        String reason,
        Map<String, Object> data
    ) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorResponse(status, errorCode, reason, data));
    }
}
