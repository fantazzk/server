package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.Room;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메인 화면에서 보여주는 참가 가능한 방 요약")
public record JoinableRoomResponse(
    @Schema(description = "방 코드", example = "ROOM01")
    String code,
    @Schema(description = "FE가 관리하는 게임 타입 메타데이터", example = "LEAGUE_OF_LEGENDS", nullable = true)
    String gameType,
    @Schema(description = "게임 모드", example = "DRAFT", allowableValues = {"DRAFT", "AUCTION"})
    String mode,
    @Schema(description = "총 팀 수", example = "2")
    int teamCount,
    @Schema(description = "현재 참가한 팀장 수", example = "1")
    int joinedLeaderCount,
    @Schema(description = "남은 팀장 자리 수", example = "1")
    int remainingSlotCount,
    @Schema(
        description = "시작 가능 여부 요약",
        example = "WAITING_FOR_DRAFT_POSITIONS",
        allowableValues = {"WAITING_FOR_LEADERS", "WAITING_FOR_DRAFT_POSITIONS", "STARTABLE", "NOT_WAITING"}
    )
    String startReadiness
) {
    public static JoinableRoomResponse from(Room room) {
        int joinedLeaderCount = room.getLeaders().size();
        return new JoinableRoomResponse(
            room.getCode(),
            room.getGameType(),
            room.getMode().name(),
            room.getTeamCount(),
            joinedLeaderCount,
            room.getTeamCount() - joinedLeaderCount,
            room.getStartReadiness().name()
        );
    }
}
