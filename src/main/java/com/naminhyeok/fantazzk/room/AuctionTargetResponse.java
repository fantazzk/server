package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 경매 대상 선수")
record AuctionTargetResponse(
    @Schema(description = "선수 이름", example = "선수2")
    String name,
    @Schema(description = "선수 포지션", example = "JUNGLE")
    String position
) {
    static AuctionTargetResponse from(RoomPlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.getName(), player.getPosition());
    }

    static AuctionTargetResponse from(GamePlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.name(), player.position());
    }
}
