package com.naminhyeok.fantazzk.template.support;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.application.CreateTemplate;
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateFixture {
    private final CreateTemplate createTemplate;

    public record PlayerSpec(String name, String position) {
    }

    public UUID createAuctionTemplateId(String name, int teamCount, int teamSize, int budget, List<PlayerSpec> players) {
        return createTemplate.create(
            new CreateTemplateCommand.Auction(
                name,
                "LEAGUE_OF_LEGENDS",
                teamCount,
                teamSize,
                budget,
                45,
                10,
                toPlayers(players)
            )
        ).getId().templateId();
    }

    public UUID createDraftTemplateId(
        String name,
        int teamCount,
        int teamSize,
        TemplateCatalog.DraftOrderStrategy strategy,
        List<PlayerSpec> players
    ) {
        return createTemplate.create(
            new CreateTemplateCommand.Draft(
                name,
                "LEAGUE_OF_LEGENDS",
                teamCount,
                teamSize,
                30,
                strategy,
                toPlayers(players)
            )
        ).getId().templateId();
    }

    private static List<CreateTemplateCommand.Player> toPlayers(List<PlayerSpec> players) {
        return IntStream.range(0, players.size())
            .mapToObj(index -> new CreateTemplateCommand.Player(players.get(index).name(), players.get(index).position(), index))
            .toList();
    }
}
