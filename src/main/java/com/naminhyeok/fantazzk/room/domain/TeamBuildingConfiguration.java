package com.naminhyeok.fantazzk.room.domain;

import java.util.Objects;

public sealed interface TeamBuildingConfiguration
    permits TeamBuildingConfiguration.Auction, TeamBuildingConfiguration.Draft {

    TeamBuildingMode mode();

    int teamCount();

    int teamSize();

    static TeamBuildingConfiguration from(Room room) {
        if (room.getMode() == TeamBuildingMode.AUCTION) {
            Integer budget = room.getBudget();
            return new Auction(
                room.getTeamCount(),
                room.getTeamSize(),
                Objects.requireNonNull(budget, "경매 모드에서는 예산이 존재해야 합니다")
            );
        }

        return new Draft(
            room.getTeamCount(),
            room.getTeamSize(),
            Objects.requireNonNull(room.getDraftOrderStrategy(), "드래프트 모드에서는 순서 전략이 존재해야 합니다")
        );
    }

    record Auction(
        int teamCount,
        int teamSize,
        int budget
    ) implements TeamBuildingConfiguration {
        @Override
        public TeamBuildingMode mode() {
            return TeamBuildingMode.AUCTION;
        }
    }

    record Draft(
        int teamCount,
        int teamSize,
        DraftOrderStrategy strategy
    ) implements TeamBuildingConfiguration {
        @Override
        public TeamBuildingMode mode() {
            return TeamBuildingMode.DRAFT;
        }
    }
}
