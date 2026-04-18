package com.naminhyeok.fantazzk.room;

record GameMemberResponse(
    String teamLeaderId,
    String playerName,
    int assignOrder
) {
    static GameMemberResponse from(RosterMember member) {
        return new GameMemberResponse(
            member.teamLeaderId().value(),
            member.playerName(),
            member.assignOrder()
        );
    }
}
