package com.naminhyeok.fantazzk.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.jmolecules.ddd.annotation.Service
@RequiredArgsConstructor
class CreateTemplate {
    private final Templates templates;

    @Transactional
    public Template create(CreateTemplateCommand command) {
        Template template = switch (command) {
            case CreateTemplateCommand.Auction auction ->
                Template.createAuction(
                    auction.name(),
                    auction.gameType(),
                    auction.teamCount(),
                    auction.teamSize(),
                    auction.budget(),
                    auction.pickBanTime(),
                    auction.minBidUnit(),
                    auction.positionLimit(),
                    auction.players().stream()
                        .map(player -> new TemplatePlayer(player.name(), player.position(), player.displayOrder()))
                        .toList()
                );
            case CreateTemplateCommand.Draft draft ->
                Template.createDraft(
                    draft.name(),
                    draft.gameType(),
                    draft.teamCount(),
                    draft.teamSize(),
                    draft.pickBanTime(),
                    draft.strategy(),
                    draft.players().stream()
                        .map(player -> new TemplatePlayer(player.name(), player.position(), player.displayOrder()))
                        .toList()
                );
        };

        return templates.save(template);
    }
}
