package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import com.naminhyeok.fantazzk.template.repository.Templates;
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
