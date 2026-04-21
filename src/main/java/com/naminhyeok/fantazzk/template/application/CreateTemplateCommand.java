package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.List;

public sealed interface CreateTemplateCommand permits CreateTemplateCommand.Auction, CreateTemplateCommand.Draft {
    String name();

    String gameType();

    int teamCount();

    int teamSize();

    int pickBanTime();

    List<Player> players();

    record Player(String name, String position, int displayOrder) {
    }

    record Auction(
        String name,
        String gameType,
        int teamCount,
        int teamSize,
        int budget,
        int pickBanTime,
        int minBidUnit,
        List<Player> players
    ) implements CreateTemplateCommand {
    }

    record Draft(
        String name,
        String gameType,
        int teamCount,
        int teamSize,
        int pickBanTime,
        TemplateCatalog.DraftOrderStrategy strategy,
        List<Player> players
    ) implements CreateTemplateCommand {
    }
}
