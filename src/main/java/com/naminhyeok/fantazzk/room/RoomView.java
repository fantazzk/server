package com.naminhyeok.fantazzk.room;

import java.util.List;

public record RoomView(
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
    DraftOrderPreviewView draftOrderPreview,
    List<TeamLeaderView> teamLeaders,
    List<RoomPlayerView> players
) {
    static RoomView from(Room room) {
        return new RoomView(
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
            DraftOrderPreviewView.from(room),
            room.getLeaders().stream().map(TeamLeaderView::from).toList(),
            room.getPlayers().stream().map(RoomPlayerView::from).toList()
        );
    }
}
