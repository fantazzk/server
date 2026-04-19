package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.RoomPlayer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 경매 대상 선수")
public record AuctionTargetResponse(
    @Schema(description = "선수 이름", example = "선수2")
    String name,
    @Schema(description = "선수 포지션", example = "JUNGLE")
    String position
) {
    public static AuctionTargetResponse from(RoomPlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.getName(), player.getPosition());
    }

    public static AuctionTargetResponse from(GamePlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.name(), player.position());
    }
}
