package com.naminhyeok.fantazzk.room;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    static RoomResponse from(RoomDetails details) {
        Room room = details.room();
        Game game = details.game();
        AuctionGame auctionGame = game instanceof AuctionGame liveAuctionGame ? liveAuctionGame : null;
        DraftGame draftGame = game instanceof DraftGame liveDraftGame ? liveDraftGame : null;

        return new RoomResponse(
            room.getCode(),
            resolveStatus(room, game),
            room.getMode().name(),
            room.getTeamCount(),
            room.getTeamSize(),
            room.getBudget(),
            room.getMinBidUnit(),
            room.getDraftOrderStrategy() == null ? null : room.getDraftOrderStrategy().name(),
            room.getStartReadiness().name(),
            DraftOrderPreviewResponse.from(room),
            leaderResponses(room, auctionGame),
            playerResponses(room, auctionGame, draftGame),
            memberResponses(room, auctionGame, draftGame),
            RoomProgressResponse.from(details)
        );
    }

    static RoomResponse from(Room room) {
        return from(RoomDetails.from(room));
    }

    private static String resolveStatus(Room room, Game game) {
        if (room.getStatus() != RoomStatus.STARTED || game == null) {
            return room.getStatus().name();
        }
        return game.getStatus().name();
    }

    private static List<TeamLeaderResponse> leaderResponses(Room room, AuctionGame auctionGame) {
        if (auctionGame == null) {
            return room.getLeaders().stream().map(TeamLeaderResponse::from).toList();
        }
        Map<TeamLeaderId, GameParticipant> participants =
            auctionGame.getParticipants().stream().collect(Collectors.toMap(GameParticipant::teamLeaderId, Function.identity()));
        return room.getLeaders().stream().map(leader -> TeamLeaderResponse.from(leader, participants.get(leader.getId()))).toList();
    }

    private static List<RoomPlayerResponse> playerResponses(Room room, AuctionGame auctionGame, DraftGame draftGame) {
        if (auctionGame == null && draftGame == null) {
            return room.getPlayers().stream().map(RoomPlayerResponse::from).toList();
        }
        if (draftGame != null) {
            Set<String> assignedPlayerNames =
                draftGame.getMembers().stream().map(RoomTeamMember::playerName).collect(Collectors.toSet());
            return room.getPlayers().stream()
                .map(player -> RoomPlayerResponse.from(player, player.getDisplayOrder(), assignedPlayerNames.contains(player.getName())))
                .toList();
        }
        Map<String, Integer> liveDisplayOrders = buildLiveDisplayOrders(auctionGame);
        Set<String> assignedPlayerNames =
            auctionGame.getMembers().stream().map(RoomTeamMember::playerName).collect(Collectors.toSet());
        return room.getPlayers().stream()
            .sorted(Comparator.comparingInt(player -> liveDisplayOrders.getOrDefault(player.getName(), Integer.MAX_VALUE)))
            .map(
                player -> RoomPlayerResponse.from(
                    player,
                    liveDisplayOrders.getOrDefault(player.getName(), player.getDisplayOrder()),
                    assignedPlayerNames.contains(player.getName())
                )
            )
            .toList();
    }

    private static Map<String, Integer> buildLiveDisplayOrders(AuctionGame auctionGame) {
        return auctionGame.getPlayerPool().stream()
            .collect(Collectors.toMap(GamePlayer::name, player -> auctionGame.getPlayerPool().indexOf(player)));
    }

    private static List<RoomMemberResponse> memberResponses(Room room, AuctionGame auctionGame, DraftGame draftGame) {
        if (auctionGame == null && draftGame == null) {
            return List.of();
        }
        if (draftGame != null) {
            return draftGame.getMembers().stream().map(RoomMemberResponse::from).toList();
        }
        return auctionGame.getMembers().stream().map(RoomMemberResponse::from).toList();
    }
}
