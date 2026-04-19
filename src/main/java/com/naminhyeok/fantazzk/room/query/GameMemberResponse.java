package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.RosterMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "확정된 팀 편성 결과")
public record GameMemberResponse(
    @Schema(description = "선수를 배정받은 팀장 ID", example = "leader-host")
    String teamLeaderId,
    @Schema(description = "배정된 선수 이름", example = "선수1")
    String playerName,
    @Schema(description = "배정 순서", example = "0")
    int assignOrder
) {
    public static GameMemberResponse from(RosterMember member) {
        return new GameMemberResponse(
            member.teamLeaderId().value(),
            member.playerName(),
            member.assignOrder()
        );
    }
}
