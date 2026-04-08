package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateFixture {
    private final CreateTemplate createTemplate;

    public UUID createAuctionTemplateId(String name, int teamCount, int teamSize, int budget, List<String> playerNames) {
        return createTemplate.create(
            new CreateTemplateCommand.Auction(name, teamCount, teamSize, budget, playerNames)
        ).getId().templateId();
    }

    public UUID createDraftTemplateId(
        String name,
        int teamCount,
        int teamSize,
        TemplateCatalog.DraftOrderStrategy strategy,
        List<String> playerNames
    ) {
        return createTemplate.create(
            new CreateTemplateCommand.Draft(
                name,
                teamCount,
                teamSize,
                DraftOrderStrategy.valueOf(strategy.name()),
                playerNames
            )
        ).getId().templateId();
    }
}
