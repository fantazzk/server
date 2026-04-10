package com.naminhyeok.fantazzk.room;

record RoomMemberResponse(
    String teamLeaderId,
    String playerName,
    int assignOrder
) {
    static RoomMemberResponse from(RoomTeamMember member) {
        return new RoomMemberResponse(
            member.teamLeaderId().value(),
            member.playerName(),
            member.assignOrder()
        );
    }
}
