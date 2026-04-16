package com.naminhyeok.fantazzk.room;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        List<RoomPlayer> players = room.getPlayers();
        Map<RoomPlayerId, RoomPlayer> playersById = players.stream()
            .collect(Collectors.toMap(RoomPlayer::getId, Function.identity()));
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
            players.stream().map(RoomPlayerResponse::from).toList(),
            room.getMembers().stream()
                .map(member -> {
                    RoomPlayer player = playersById.get(member.playerId());
                    return RoomMemberResponse.from(member, player == null ? room.findPlayer(member.playerId()) : player);
                })
                .toList(),
            RoomProgressResponse.from(room)
        );
    }
}
