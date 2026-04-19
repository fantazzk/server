package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "방 참가 요청")
record JoinRoomRequest(
    @Schema(description = "참가자 닉네임", example = "게스트", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다") String nickname
) {
}
