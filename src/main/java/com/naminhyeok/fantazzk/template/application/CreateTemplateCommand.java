package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import java.util.List;

public sealed interface CreateTemplateCommand permits CreateTemplateCommand.Auction, CreateTemplateCommand.Draft {
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
