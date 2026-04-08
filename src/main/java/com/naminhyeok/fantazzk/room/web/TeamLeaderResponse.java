package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.TeamLeaderView;

record TeamLeaderResponse(
    String id,
    String nickname,
    Integer remainingBudget
) {
    static TeamLeaderResponse from(TeamLeaderView leader) {
        return new TeamLeaderResponse(leader.id(), leader.nickname(), leader.remainingBudget());
    }
}
