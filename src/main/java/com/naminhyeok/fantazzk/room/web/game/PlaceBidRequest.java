package com.naminhyeok.fantazzk.room.web.game;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

record PlaceBidRequest(
    @NotNull(message = "입찰 금액은 필수입니다")
    @Positive(message = "입찰 금액은 1 이상이어야 합니다") Integer amount
) {
}
