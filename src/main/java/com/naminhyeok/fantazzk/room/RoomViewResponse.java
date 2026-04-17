package com.naminhyeok.fantazzk.room;

import java.util.List;

record RoomViewResponse(
    String code,
    String status,
    String mode,
    int teamCount,
    int teamSize,
    Integer budget,
    Integer minBidUnit,
    String draftOrderStrategy,
    String startReadiness,
    String startedGameId,
    DraftOrderPreviewResponse draftOrderPreview,
    List<TeamLeaderResponse> teamLeaders,
    List<RoomPlayerResponse> players
) {
    static RoomViewResponse from(Room room) {
        return new RoomViewResponse(
            room.getCode(),
            room.getStatus().name(),
            room.getMode().name(),
            room.getTeamCount(),
            room.getTeamSize(),
            room.getBudget(),
            room.getMinBidUnit(),
            room.getDraftOrderStrategy() == null ? null : room.getDraftOrderStrategy().name(),
            room.getStartReadiness().name(),
            room.getStartedGameId() == null ? null : room.getStartedGameId().gameId().toString(),
            DraftOrderPreviewResponse.from(room),
            room.getLeaders().stream().map(TeamLeaderResponse::from).toList(),
            room.getPlayers().stream().map(RoomPlayerResponse::from).toList()
        );
    }
}
