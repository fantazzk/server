package com.naminhyeok.fantazzk.room;

public record GameMemberView(
    String teamLeaderId,
    String playerName,
    int assignOrder
) {
    static GameMemberView from(RosterMember member) {
        return new GameMemberView(
            member.teamLeaderId().value(),
            member.playerName(),
            member.assignOrder()
        );
    }
}
