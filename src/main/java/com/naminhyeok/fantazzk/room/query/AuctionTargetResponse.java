package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 경매 대상 선수")
public record AuctionTargetResponse(
    @Schema(description = "선수 이름", example = "선수2")
    String name,
    @Schema(description = "FE가 관리하는 선수 포지션 메타데이터", example = "JUNGLE", nullable = true)
    String position
) {
    public static AuctionTargetResponse from(GamePlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.name(), player.position());
    }
}
