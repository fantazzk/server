package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 사용자에게 발급된 방 세션 정보")
public record TeamLeaderSessionResponse(
    @Schema(description = "현재 사용자 팀장 ID", example = "leader-host")
    String leaderId,
    @Schema(description = "현재 사용자 역할. HOST 만 방 시작 권한을 가집니다.", example = "HOST")
    Role role,
    @Schema(
        description = "이후 모든 mutation 요청의 `X-Room-Action-Token` 헤더에 넣어야 하는 값입니다. "
            + "로그인 토큰이 아니라 방 액션 전용 토큰입니다.",
        example = "room-action-token"
    )
    String actionToken
) {
    public static TeamLeaderSessionResponse from(Room room, RoomTeamLeader leader) {
        return new TeamLeaderSessionResponse(
            leader.getId().value(),
            room.getHostLeaderId().equals(leader.getId()) ? Role.HOST : Role.LEADER,
            leader.getActionToken()
        );
    }

    public enum Role {
        HOST,
        LEADER
    }
}
