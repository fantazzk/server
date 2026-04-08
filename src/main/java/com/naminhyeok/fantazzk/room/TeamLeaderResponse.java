package com.naminhyeok.fantazzk.room;

record TeamLeaderResponse(
    String id,
    String nickname,
    Integer remainingBudget
) {
    static TeamLeaderResponse from(RoomTeamLeader leader) {
        return new TeamLeaderResponse(leader.getTeamLeaderId(), leader.getNickname(), leader.getRemainingBudget());
    }
}
