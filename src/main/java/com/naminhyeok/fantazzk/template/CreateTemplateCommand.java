package com.naminhyeok.fantazzk.template;

import java.util.List;

sealed interface CreateTemplateCommand permits CreateTemplateCommand.Auction, CreateTemplateCommand.Draft {
    String name();

    int teamCount();

    int teamSize();

    List<String> playerNames();

    record Auction(
        String name,
        int teamCount,
        int teamSize,
        int budget,
        List<String> playerNames
    ) implements CreateTemplateCommand {
    }

    record Draft(
        String name,
        int teamCount,
        int teamSize,
        DraftOrderStrategy strategy,
        List<String> playerNames
    ) implements CreateTemplateCommand {
    }
}
