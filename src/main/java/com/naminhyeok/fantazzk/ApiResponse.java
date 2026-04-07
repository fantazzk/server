package com.naminhyeok.fantazzk;

import lombok.Getter;

@Getter
public final class ApiResponse<T> {
    private final String resultType;
    private final T success;
    private final ErrorResponse error;

    private ApiResponse(String resultType, T success, ErrorResponse error) {
        this.resultType = resultType;
        this.success = success;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T success) {
        return new ApiResponse<>("SUCCESS", success, null);
    }

    public static ApiResponse<Void> error(int status, String errorCode, String reason) {
        return new ApiResponse<>("ERROR", null, new ErrorResponse(status, errorCode, reason));
    }
}
