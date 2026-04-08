package com.naminhyeok.fantazzk.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record CreateRoomRequest(
    @NotNull(message = "템플릿 ID는 필수입니다") UUID templateId,
    @NotBlank(message = "호스트 이름은 비어 있을 수 없습니다") String hostNickname
) {
}
