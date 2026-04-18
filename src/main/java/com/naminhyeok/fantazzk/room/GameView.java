package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record GameView(
    String id,
    String roomCode,
    String mode,
    String status,
    int teamCount,
    int teamSize,
    Integer budget,
    Integer minBidUnit,
    String draftOrderStrategy,
    List<GameParticipantView> participants,
    List<GamePlayerView> players,
    List<GameMemberView> members,
    GameProgressView progress
) {
    public static GameView from(Game game) {
        return new GameView(
            game.getId().gameId().toString(),
            game.getRoomCode(),
            modeOf(game),
            game.getStatus().name(),
            game.getTeamCount(),
            game.getTeamSize(),
            game.getBudget(),
            game.getMinBidUnit(),
            game.getDraftOrderStrategy() == null ? null : game.getDraftOrderStrategy().name(),
            game.getParticipants().stream().map(GameParticipantView::from).toList(),
            playerViews(game),
            memberViews(game),
            GameProgressView.from(game)
        );
    }

    private static String modeOf(Game game) {
        return game instanceof AuctionGame ? RoomMode.AUCTION.name() : RoomMode.DRAFT.name();
    }

    private static List<GamePlayerView> playerViews(Game game) {
        Set<String> assignedPlayerNames = assignedPlayerNamesOf(game);
        if (game instanceof AuctionGame) {
            List<GamePlayer> playerPool = game.getPlayerPool();
            return playerPool.stream()
                .map(player -> GamePlayerView.from(
                    player,
                    playerPool.indexOf(player),
                    assignedPlayerNames.contains(player.name())
                ))
                .toList();
        }
        return game.getPlayerPool().stream()
            .sorted(Comparator.comparingInt(GamePlayer::displayOrder))
            .map(player -> GamePlayerView.from(
                player,
                player.displayOrder(),
                assignedPlayerNames.contains(player.name())
            ))
            .toList();
    }

    private static Set<String> assignedPlayerNamesOf(Game game) {
        return membersOf(game).stream().map(RosterMember::playerName).collect(Collectors.toSet());
    }

    private static List<GameMemberView> memberViews(Game game) {
        return membersOf(game).stream().map(GameMemberView::from).toList();
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
