package com.naminhyeok.fantazzk.room;

record TeamLeaderResponse(
    String id,
    String nickname,
    Integer draftPosition,
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

    static TeamLeaderResponse from(RoomTeamLeader leader, GameParticipant participant) {
        if (participant == null) {
            return from(leader);
        }
        return new TeamLeaderResponse(
            leader.getId().value(),
            leader.getNickname(),
            participant.draftPosition(),
            participant.remainingBudget()
        );
    }
}
