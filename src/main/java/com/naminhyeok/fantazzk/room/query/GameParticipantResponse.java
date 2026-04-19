package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게임 참여 팀장 정보")
public record GameParticipantResponse(
    @Schema(description = "팀장 ID", example = "leader-host")
    String teamLeaderId,
    @Schema(description = "닉네임", example = "호스트")
    String nickname,
    @Schema(description = "드래프트 순번. 경매 모드에서는 null", example = "1", nullable = true)
    Integer draftPosition,
    @Schema(description = "남은 예산. 드래프트 모드에서는 null", example = "300", nullable = true)
    Integer remainingBudget
) {
    public static GameParticipantResponse from(GameParticipant participant) {
        return new GameParticipantResponse(
            participant.teamLeaderId().value(),
            participant.nickname(),
            participant.draftPosition(),
            participant.remainingBudget()
        );
    }
}
