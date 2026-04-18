package com.naminhyeok.fantazzk.room;

public record JoinableRoomView(
    String code,
    String mode,
    int teamCount,
    int joinedLeaderCount,
    int remainingSlotCount,
    String startReadiness
) {
    static JoinableRoomView from(Room room) {
        int joinedLeaderCount = room.getLeaders().size();
        return new JoinableRoomView(
            room.getCode(),
            room.getMode().name(),
            room.getTeamCount(),
            joinedLeaderCount,
            room.getTeamCount() - joinedLeaderCount,
            room.getStartReadiness().name()
        );
    }
}
