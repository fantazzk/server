package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.PlayerStatus;
import com.naminhyeok.fantazzk.room.domain.RoomPlayer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로비에 노출되는 선수 정보")
public record RoomPlayerResponse(
    @Schema(description = "선수 이름", example = "선수1")
    String name,
    @Schema(description = "선수 포지션", example = "TOP")
    String position,
    @Schema(description = "노출 순서", example = "0")
    int displayOrder,
    @Schema(description = "배정 상태", example = "AVAILABLE", allowableValues = {"AVAILABLE", "ASSIGNED"})
    String status
) {
    public static RoomPlayerResponse from(RoomPlayer player) {
        return new RoomPlayerResponse(
            player.getName(),
            player.getPosition(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }

    public static RoomPlayerResponse from(RoomPlayer player, int displayOrder, boolean assigned) {
        return new RoomPlayerResponse(
            player.getName(),
            player.getPosition(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
