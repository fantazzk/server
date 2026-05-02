package com.naminhyeok.fantazzk.room.domain;

import java.util.Comparator;

final class StartedGameSnapshotFactory {
    private StartedGameSnapshotFactory() {
    }

    static StartedGameSnapshot from(Room room) {
        return new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            room.getStartedAt(),
            room.getGameType(),
            room.getMode(),
            rulesOf(room),
            room.getLeaders().stream()
                .map(leader -> room.getMode() == RoomMode.AUCTION
                    ? GameParticipant.auction(leader.getId(), leader.getNickname(), leader.getRemainingBudget())
                    : GameParticipant.draft(leader.getId(), leader.getNickname(), leader.getDraftPosition()))
                .toList(),
            room.getPlayers().stream()
                .sorted(Comparator.comparingInt(RoomPlayer::getDisplayOrder))
                .map(player -> new GamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                .toList()
        );
    }

    private static GameRules rulesOf(Room room) {
        if (room.getMode() == RoomMode.AUCTION) {
            return GameRules.auction(
                room.getTeamCount(),
                room.getTeamSize(),
                room.getBudget(),
                room.getPickBanTime(),
                room.getMinBidUnit()
            );
        }
        return GameRules.draft(
            room.getTeamCount(),
            room.getTeamSize(),
            room.getPickBanTime(),
            room.getDraftOrderStrategy()
        );
    }
}
