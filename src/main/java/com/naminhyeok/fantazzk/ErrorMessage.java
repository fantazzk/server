package com.naminhyeok.fantazzk;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답 payload")
public record ErrorMessage(
    @Schema(description = "에러 코드", example = "ROOM_NOT_FOUND")
    String code,
    @Schema(description = "사용자 노출용 에러 메시지", example = "방을 찾을 수 없습니다")
    String message,
    @Schema(description = "추가 진단 정보 또는 validation 필드 에러 맵. 없으면 null 입니다.")
    Object data
) {
    public ErrorMessage(ErrorDescriptor descriptor) {
        this(descriptor, null);
    }

    public ErrorMessage(ErrorDescriptor descriptor, Object data) {
        this(descriptor.getCode(), descriptor.getMessage(), data);
    }
}
