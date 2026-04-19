package com.naminhyeok.fantazzk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "모든 API가 공통으로 사용하는 응답 envelope 입니다. 성공이면 `success`, 실패이면 `error` 를 확인합니다.")
public final class ApiResponse<T> {
    @Schema(description = "응답 결과 타입", implementation = ResultType.class)
    private final ResultType resultType;
    @Schema(description = "성공 응답 payload. 실패 시 null 입니다.")
    private final T success;
    @Schema(description = "실패 응답 정보. 성공 시 null 입니다.")
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
