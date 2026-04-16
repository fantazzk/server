package com.naminhyeok.fantazzk.room;

record RoomMemberResponse(
    String teamLeaderId,
    int playerId,
    String playerName,
    int assignOrder
) {
    static RoomMemberResponse from(RoomTeamMember member, RoomPlayer player) {
        return new RoomMemberResponse(
            member.teamLeaderId().value(),
            member.playerId().value(),
            player.getName(),
            member.assignOrder()
        );
    }
}
