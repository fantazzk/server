package com.naminhyeok.fantazzk;

import lombok.Getter;

@Getter
public final class ApiResponse<T> {
    private final ResultType resultType;
    private final T success;
    private final ErrorMessage error;

    private ApiResponse(ResultType resultType, T success, ErrorMessage error) {
        this.resultType = resultType;
        this.success = success;
        this.error = error;
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <T> ApiResponse<T> success(T success) {
        return new ApiResponse<>(ResultType.SUCCESS, success, null);
    }

    public static ApiResponse<Void> error(ErrorDescriptor errorDescriptor) {
        return error(errorDescriptor, null);
    }

    public static ApiResponse<Void> error(ErrorDescriptor errorDescriptor, Object data) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorDescriptor, data));
    }
}
