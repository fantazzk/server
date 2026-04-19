package com.naminhyeok.fantazzk.room.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "드래프트 자리 선택 요청")
public record SelectDraftPositionRequest(
    @Schema(
        description = "선택할 드래프트 순번. 1부터 teamCount 까지의 정수",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "드래프트 자리는 필수입니다") Integer draftPosition
) {
}
