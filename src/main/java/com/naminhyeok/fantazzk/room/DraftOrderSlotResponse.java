package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "드래프트 순번별 배정 현황")
record DraftOrderSlotResponse(
    @Schema(description = "드래프트 순번", example = "1")
    int draftPosition,
    @Schema(description = "이 순번을 점유한 팀장 ID. 비어 있으면 null", example = "leader-host", nullable = true)
    String leaderId,
    @Schema(description = "이 순번을 점유한 닉네임. 비어 있으면 null", example = "호스트", nullable = true)
    String nickname
) {
    static DraftOrderSlotResponse empty(int draftPosition) {
        return new DraftOrderSlotResponse(draftPosition, null, null);
    }

    static DraftOrderSlotResponse from(int draftPosition, RoomTeamLeader leader) {
        return new DraftOrderSlotResponse(
            draftPosition,
            leader.getId().value(),
            leader.getNickname()
        );
    }
}
