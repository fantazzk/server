package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "경매 입찰 요청")
record PlaceBidRequest(
    @Schema(description = "입찰 금액. 현재 최고가보다 높고 최소 증가폭을 만족해야 합니다.", example = "150")
    @NotNull(message = "입찰 금액은 필수입니다")
    @Positive(message = "입찰 금액은 1 이상이어야 합니다") Integer amount
) {
}
