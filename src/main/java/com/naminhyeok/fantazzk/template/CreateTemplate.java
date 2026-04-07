package com.naminhyeok.fantazzk.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTemplate {
    private final Templates templates;

    @Transactional
    public Template create(CreateTemplateCommand command) {
        Template template = switch (command) {
            case CreateTemplateCommand.Auction auction ->
                Template.createAuction(
                    auction.name(),
                    auction.teamCount(),
                    auction.teamSize(),
                    auction.budget(),
                    auction.playerNames()
                );
            case CreateTemplateCommand.Draft draft ->
                Template.createDraft(
                    draft.name(),
                    draft.teamCount(),
                    draft.teamSize(),
                    draft.strategy(),
                    draft.playerNames()
                );
        };

        return templates.save(template);
    }
}
