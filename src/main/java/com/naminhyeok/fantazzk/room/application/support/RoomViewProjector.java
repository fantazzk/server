package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.AuctionTargetView;
import com.naminhyeok.fantazzk.room.DraftOrderPreviewView;
import com.naminhyeok.fantazzk.room.DraftOrderSlotView;
import com.naminhyeok.fantazzk.room.GameMemberView;
import com.naminhyeok.fantazzk.room.GameParticipantView;
import com.naminhyeok.fantazzk.room.GamePlayerView;
import com.naminhyeok.fantazzk.room.GameProgressView;
import com.naminhyeok.fantazzk.room.GameView;
import com.naminhyeok.fantazzk.room.RoomPlayerView;
import com.naminhyeok.fantazzk.room.RoomView;
import com.naminhyeok.fantazzk.room.TeamLeaderView;
import com.naminhyeok.fantazzk.room.domain.game.AuctionBid;
import com.naminhyeok.fantazzk.room.domain.game.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.game.DraftGame;
import com.naminhyeok.fantazzk.room.domain.game.DraftProgress;
import com.naminhyeok.fantazzk.room.domain.game.Game;
import com.naminhyeok.fantazzk.room.domain.game.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.game.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.game.GameStatus;
import com.naminhyeok.fantazzk.room.domain.game.RosterMember;
import com.naminhyeok.fantazzk.room.domain.room.Room;
import com.naminhyeok.fantazzk.room.domain.room.RoomPlayer;
import com.naminhyeok.fantazzk.room.domain.room.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.shared.PlayerStatus;
import com.naminhyeok.fantazzk.room.domain.shared.RoomMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RoomViewProjector {
    private RoomViewProjector() {}

    public static RoomView toRoomView(Room room) {
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
            toDraftOrderPreviewView(room),
            room.getLeaders().stream().map(RoomViewProjector::toTeamLeaderView).toList(),
            room.getPlayers().stream().map(RoomViewProjector::toRoomPlayerView).toList()
        );
    }

    public static GameView toGameView(Game game) {
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
            game.getParticipants().stream().map(RoomViewProjector::toGameParticipantView).toList(),
            toGamePlayerViews(game),
            toGameMemberViews(game),
            toGameProgressView(game)
        );
    }

    private static TeamLeaderView toTeamLeaderView(RoomTeamLeader leader) {
        return new TeamLeaderView(
            leader.getId().value(),
            leader.getNickname(),
            leader.getDraftPosition(),
            leader.getRemainingBudget()
        );
    }

    private static RoomPlayerView toRoomPlayerView(RoomPlayer player) {
        return new RoomPlayerView(
            player.getName(),
            player.getPosition(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }

    private static DraftOrderPreviewView toDraftOrderPreviewView(Room room) {
        if (room.getMode() != RoomMode.DRAFT) {
            return null;
        }

        Map<Integer, RoomTeamLeader> leadersByDraftPosition = new HashMap<>();
        room.getLeaders().stream()
            .filter(leader -> leader.getDraftPosition() != null)
            .forEach(leader -> leadersByDraftPosition.put(leader.getDraftPosition(), leader));

        List<DraftOrderSlotView> slots = IntStream.rangeClosed(1, room.getTeamCount())
            .mapToObj(draftPosition -> {
                RoomTeamLeader leader = leadersByDraftPosition.get(draftPosition);
                if (leader == null) {
                    return new DraftOrderSlotView(draftPosition, null, null);
                }
                return new DraftOrderSlotView(
                    draftPosition,
                    leader.getId().value(),
                    leader.getNickname()
                );
            })
            .toList();

        return new DraftOrderPreviewView(slots);
    }

    private static String modeOf(Game game) {
        return game instanceof AuctionGame ? RoomMode.AUCTION.name() : RoomMode.DRAFT.name();
    }

    private static GameParticipantView toGameParticipantView(GameParticipant participant) {
        return new GameParticipantView(
            participant.teamLeaderId().value(),
            participant.nickname(),
            participant.draftPosition(),
            participant.remainingBudget()
        );
    }

    private static List<GamePlayerView> toGamePlayerViews(Game game) {
        Set<String> assignedPlayerNames = assignedPlayerNamesOf(game);
        if (game instanceof AuctionGame auctionGame) {
            List<GamePlayer> playerPool = auctionGame.getPlayerPool();
            return playerPool.stream()
                .map(player -> new GamePlayerView(
                    player.name(),
                    player.position(),
                    playerPool.indexOf(player),
                    assignedPlayerNames.contains(player.name()) ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
                ))
                .toList();
        }
        return game.getPlayerPool().stream()
            .sorted(Comparator.comparingInt(GamePlayer::displayOrder))
            .map(player -> new GamePlayerView(
                player.name(),
                player.position(),
                player.displayOrder(),
                assignedPlayerNames.contains(player.name()) ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
            ))
            .toList();
    }

    private static List<GameMemberView> toGameMemberViews(Game game) {
        return membersOf(game).stream()
            .map(member -> new GameMemberView(
                member.teamLeaderId().value(),
                member.playerName(),
                member.assignOrder()
            ))
            .toList();
    }

    private static GameProgressView toGameProgressView(Game game) {
        if (game instanceof AuctionGame auctionGame) {
            if (auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
                return emptyGameProgressView();
            }
            AuctionBid winningBid = auctionGame.currentWinningBid();
            GamePlayer auctionTarget = auctionGame.currentAuctionTarget();
            return new GameProgressView(
                null,
                auctionGame.getCurrentRound() <= 0 ? null : auctionGame.getCurrentRound(),
                null,
                null,
                auctionGame.getCurrentRoundEndsAt(),
                auctionTarget == null ? null : new AuctionTargetView(auctionTarget.name(), auctionTarget.position()),
                winningBid == null ? null : winningBid.amount(),
                winningBid == null ? null : winningBid.teamLeaderId().value(),
                auctionGame.currentBidCount()
            );
        }
        if (game instanceof DraftGame draftGame) {
            if (draftGame.getStatus() != GameStatus.IN_PROGRESS) {
                return emptyGameProgressView();
            }
            DraftProgress progress = draftGame.currentDraftProgress();
            return new GameProgressView(
                draftGame.getCurrentTurnIndex(),
                progress.currentRound(),
                progress.currentLeaderId(),
                progress.currentRoundLeaderIds(),
                null,
                null,
                null,
                null,
                null
            );
        }
        return emptyGameProgressView();
    }

    private static Set<String> assignedPlayerNamesOf(Game game) {
        return membersOf(game).stream()
            .map(RosterMember::playerName)
            .collect(Collectors.toSet());
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

    private static GameProgressView emptyGameProgressView() {
        return new GameProgressView(null, null, null, null, null, null, null, null, null);
    }
}
