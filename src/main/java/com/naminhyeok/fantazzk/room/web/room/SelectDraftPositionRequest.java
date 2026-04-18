package com.naminhyeok.fantazzk.room.web.room;

import jakarta.validation.constraints.NotNull;

public record SelectDraftPositionRequest(
    @NotNull(message = "드래프트 자리는 필수입니다") Integer draftPosition
) {
}
