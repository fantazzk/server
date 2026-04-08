package com.naminhyeok.fantazzk.room.api;

import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;

public record TeamLeaderResponse(
    String id,
    String nickname,
    Integer remainingBudget
) {
    static TeamLeaderResponse from(RoomTeamLeader leader) {
        return new TeamLeaderResponse(leader.getTeamLeaderId(), leader.getNickname(), leader.getRemainingBudget());
    }
}
