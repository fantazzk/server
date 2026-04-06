package com.naminhyeok.fantazzk.room.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DraftBoard {
    private final List<String> teamLeaderIds;
    private final DraftOrderStrategy strategy;
    private final int picksPerTeam;

    public DraftBoard(List<String> teamLeaderIds, DraftOrderStrategy strategy, int picksPerTeam) {
        this.teamLeaderIds = List.copyOf(teamLeaderIds);
        this.strategy = strategy;
        this.picksPerTeam = picksPerTeam;
    }

    public List<String> getTeamLeaderIds() {
        return teamLeaderIds;
    }

    public DraftOrderStrategy getStrategy() {
        return strategy;
    }

    public int getPicksPerTeam() {
        return picksPerTeam;
    }

    public List<String> pickOrder() {
        List<String> reversed = new ArrayList<>(teamLeaderIds);
        Collections.reverse(reversed);
        List<String> order = new ArrayList<>();
        for (int round = 0; round < picksPerTeam; round++) {
            if (strategy == DraftOrderStrategy.SNAKE && round % 2 == 1) {
                order.addAll(reversed);
            } else {
                order.addAll(teamLeaderIds);
            }
        }
        return List.copyOf(order);
    }

    public String currentTeamLeader(int turnIndex) {
        require(turnIndex >= 0, "드래프트 턴은 0 이상이어야 합니다");
        List<String> order = pickOrder();
        checkState(turnIndex < order.size(), "드래프트가 이미 종료되었습니다");
        return order.get(turnIndex);
    }

    public void requireTurnOwner(int turnIndex, String teamLeaderId) {
        checkState(currentTeamLeader(turnIndex).equals(teamLeaderId), "현재 턴이 아닙니다");
    }

    public DraftPickSettlement settlePick(int turnIndex, int assignedCountAfterPick) {
        return new DraftPickSettlement(turnIndex + 1, assignedCountAfterPick >= totalPickCount());
    }

    private int totalPickCount() {
        return teamLeaderIds.size() * picksPerTeam;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
