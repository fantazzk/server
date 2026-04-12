package com.naminhyeok.fantazzk.template;

import java.util.List;

sealed interface CreateTemplateCommand permits CreateTemplateCommand.Auction, CreateTemplateCommand.Draft {
    String name();

    GameType gameType();

    int teamCount();

    int teamSize();

    int pickBanTime();

    List<Player> players();

    record Player(String name, String position, int displayOrder) {
    }

    record Auction(
        String name,
        GameType gameType,
        int teamCount,
        int teamSize,
        int budget,
        int pickBanTime,
        int minBidUnit,
        Integer positionLimit,
        List<Player> players
    ) implements CreateTemplateCommand {
    }

    record Draft(
        String name,
        GameType gameType,
        int teamCount,
        int teamSize,
        int pickBanTime,
        DraftOrderStrategy strategy,
        List<Player> players
    ) implements CreateTemplateCommand {
    }
}
