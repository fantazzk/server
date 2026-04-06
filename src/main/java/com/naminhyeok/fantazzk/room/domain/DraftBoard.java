package com.naminhyeok.fantazzk.room.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record DraftBoard(List<String> teamLeaderIds, DraftOrderStrategy strategy, int picksPerTeam) {
    public DraftBoard {
        teamLeaderIds = List.copyOf(teamLeaderIds);
    }

    public List<String> pickOrder() {
        List<String> reversedOrder = new ArrayList<>(teamLeaderIds);
        Collections.reverse(reversedOrder);

        List<String> order = new ArrayList<>();
        for (int round = 0; round < picksPerTeam; round++) {
            if (strategy == DraftOrderStrategy.SNAKE) {
                order.addAll(round % 2 == 0 ? teamLeaderIds : reversedOrder);
            } else {
                order.addAll(teamLeaderIds);
            }
        }

        return order;
    }

    public String currentTeamLeader(int turnIndex) {
        if (turnIndex < 0) {
            throw new IllegalArgumentException("드래프트 턴은 0 이상이어야 합니다");
        }

        List<String> order = pickOrder();
        if (turnIndex >= order.size()) {
            throw new IllegalStateException("드래프트가 이미 종료되었습니다");
        }

        return order.get(turnIndex);
    }

    public void requireTurnOwner(int turnIndex, String teamLeaderId) {
        if (!currentTeamLeader(turnIndex).equals(teamLeaderId)) {
            throw new IllegalStateException("현재 턴이 아닙니다");
        }
    }

    public DraftPickSettlement settlePick(int turnIndex, int assignedCountAfterPick) {
        return new DraftPickSettlement(turnIndex + 1, assignedCountAfterPick >= totalPickCount());
    }

    private int totalPickCount() {
        return teamLeaderIds.size() * picksPerTeam;
    }
}
