package com.naminhyeok.fantazzk.room;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

record PickDraftRequest(
    @NotNull(message = "선수 식별자는 필수입니다")
    @PositiveOrZero(message = "선수 식별자는 0 이상이어야 합니다") Integer playerId
) {
}
