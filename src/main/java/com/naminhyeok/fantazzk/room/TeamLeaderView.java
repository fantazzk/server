package com.naminhyeok.fantazzk.room;

public record TeamLeaderView(
    String id,
    String nickname,
    Integer remainingBudget
) {
    static TeamLeaderView from(RoomTeamLeader leader) {
        return new TeamLeaderView(leader.getTeamLeaderId(), leader.getNickname(), leader.getRemainingBudget());
    }
}
