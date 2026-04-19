package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진행 화면에 노출되는 선수 정보")
record GamePlayerResponse(
    @Schema(description = "선수 이름", example = "선수1")
    String name,
    @Schema(description = "선수 포지션", example = "TOP")
    String position,
    @Schema(description = "노출 순서. 경매 모드에서는 현재 경매 순서, 드래프트 모드에서는 원래 displayOrder 입니다.", example = "0")
    int displayOrder,
    @Schema(description = "현재 배정 여부", example = "AVAILABLE", allowableValues = {"AVAILABLE", "ASSIGNED"})
    String status
) {
    static GamePlayerResponse from(GamePlayer player, int displayOrder, boolean assigned) {
        return new GamePlayerResponse(
            player.name(),
            player.position(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
