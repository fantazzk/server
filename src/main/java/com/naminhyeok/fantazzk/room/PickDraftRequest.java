package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "드래프트 픽 요청")
record PickDraftRequest(
    @Schema(
        description = "선택할 선수 이름. `GameResponse.players` 의 사용 가능한 이름 중 하나여야 합니다.",
        example = "선수3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "선수 이름은 비어 있을 수 없습니다") String playerName
) {
}
