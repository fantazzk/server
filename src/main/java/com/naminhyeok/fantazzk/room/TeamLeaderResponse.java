package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로비 또는 게임에 참가한 팀장 정보")
record TeamLeaderResponse(
    @Schema(description = "팀장 ID", example = "leader-host")
    String id,
    @Schema(description = "닉네임", example = "호스트")
    String nickname,
    @Schema(description = "드래프트 순번. 경매 모드에서는 null", example = "1", nullable = true)
    Integer draftPosition,
    @Schema(description = "남은 예산. 드래프트 모드에서는 null", example = "300", nullable = true)
    Integer remainingBudget
) {
    static TeamLeaderResponse from(RoomTeamLeader leader) {
        return new TeamLeaderResponse(
            leader.getId().value(),
            leader.getNickname(),
            leader.getDraftPosition(),
            leader.getRemainingBudget()
        );
    }
}
