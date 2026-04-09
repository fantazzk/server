package com.naminhyeok.fantazzk.room;

import jakarta.validation.constraints.NotNull;

record SelectDraftPositionRequest(
    @NotNull(message = "드래프트 자리는 필수입니다") Integer draftPosition
) {
}
