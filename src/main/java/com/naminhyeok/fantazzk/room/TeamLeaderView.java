package com.naminhyeok.fantazzk.room;

public record TeamLeaderView(
    String id,
    String nickname,
    Integer draftPosition,
    Integer remainingBudget
) {
    static TeamLeaderView from(RoomTeamLeader leader) {
        return new TeamLeaderView(
            leader.getId().value(),
            leader.getNickname(),
            leader.getDraftPosition(),
            leader.getRemainingBudget()
        );
    }
}
