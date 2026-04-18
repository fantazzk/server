package com.naminhyeok.fantazzk.room;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

record GameResponse(
    String id,
    String roomCode,
    String mode,
    String status,
    int teamCount,
    int teamSize,
    Integer budget,
    Integer minBidUnit,
    String draftOrderStrategy,
    List<GameParticipantResponse> participants,
    List<GamePlayerResponse> players,
    List<GameMemberResponse> members,
    GameProgressResponse progress
) {
    static GameResponse from(Game game) {
        return new GameResponse(
            game.getId().gameId().toString(),
            game.getRoomCode(),
            modeOf(game),
            game.getStatus().name(),
            game.getTeamCount(),
            game.getTeamSize(),
            game.getBudget(),
            game.getMinBidUnit(),
            game.getDraftOrderStrategy() == null ? null : game.getDraftOrderStrategy().name(),
            game.getParticipants().stream().map(GameParticipantResponse::from).toList(),
            playerResponses(game),
            memberResponses(game),
            GameProgressResponse.from(game)
        );
    }

    private static String modeOf(Game game) {
        return game instanceof AuctionGame ? RoomMode.AUCTION.name() : RoomMode.DRAFT.name();
    }

    private static List<GamePlayerResponse> playerResponses(Game game) {
        Set<String> assignedPlayerNames = assignedPlayerNamesOf(game);
        if (game instanceof AuctionGame) {
            List<GamePlayer> playerPool = game.getPlayerPool();
            return playerPool.stream()
                .map(player -> GamePlayerResponse.from(
                    player,
                    playerPool.indexOf(player),
                    assignedPlayerNames.contains(player.name())
                ))
                .toList();
        }
        return game.getPlayerPool().stream()
            .sorted(Comparator.comparingInt(GamePlayer::displayOrder))
            .map(player -> GamePlayerResponse.from(
                player,
                player.displayOrder(),
                assignedPlayerNames.contains(player.name())
            ))
            .toList();
    }

    private static Set<String> assignedPlayerNamesOf(Game game) {
        return membersOf(game).stream().map(RosterMember::playerName).collect(Collectors.toSet());
    }

    private static List<GameMemberResponse> memberResponses(Game game) {
        return membersOf(game).stream().map(GameMemberResponse::from).toList();
    }

    private static List<RosterMember> membersOf(Game game) {
        if (game instanceof AuctionGame auctionGame) {
            return auctionGame.getMembers();
        }
        if (game instanceof DraftGame draftGame) {
            return draftGame.getMembers();
        }
        return List.of();
    }
}
