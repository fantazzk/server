package com.naminhyeok.fantazzk.room.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "방 생성 요청")
public record CreateRoomRequest(
    @Schema(
        description = "방 생성에 사용할 템플릿 ID",
        example = "11111111-1111-1111-1111-111111111111",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "템플릿 ID는 필수입니다") UUID templateId,
    @Schema(description = "호스트 닉네임", example = "호스트", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "호스트 이름은 비어 있을 수 없습니다") String hostNickname
) {
}
