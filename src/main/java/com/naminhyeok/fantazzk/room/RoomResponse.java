package com.naminhyeok.fantazzk.room;

import java.util.List;

record RoomResponse(
    String code,
    String status,
    String mode,
    int teamCount,
    int teamSize,
    Integer budget,
    Integer minBidUnit,
    String draftOrderStrategy,
    String startReadiness,
    DraftOrderPreviewResponse draftOrderPreview,
    List<TeamLeaderResponse> teamLeaders,
    List<RoomPlayerResponse> players,
    List<RoomMemberResponse> members,
    RoomProgressResponse progress
) {
    static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getCode(),
            room.getStatus().name(),
            room.getMode().name(),
            room.getTeamCount(),
            room.getTeamSize(),
            room.getBudget(),
            room.getMinBidUnit(),
            room.getDraftOrderStrategy() == null ? null : room.getDraftOrderStrategy().name(),
            room.getStartReadiness().name(),
            DraftOrderPreviewResponse.from(room),
            room.getLeaders().stream().map(TeamLeaderResponse::from).toList(),
            room.getPlayers().stream().map(RoomPlayerResponse::from).toList(),
            room.getMembers().stream().map(RoomMemberResponse::from).toList(),
            RoomProgressResponse.from(room)
        );
    }
}
