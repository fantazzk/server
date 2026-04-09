package com.naminhyeok.fantazzk.room;

record JoinableRoomResponse(
    String code,
    String mode,
    int teamCount,
    int joinedLeaderCount,
    int remainingSlotCount,
    String startReadiness
) {
    static JoinableRoomResponse from(Room room) {
        int joinedLeaderCount = room.getLeaders().size();
        return new JoinableRoomResponse(
            room.getCode(),
            room.getMode().name(),
            room.getTeamCount(),
            joinedLeaderCount,
            room.getTeamCount() - joinedLeaderCount,
            room.getStartReadiness().name()
        );
    }
}
