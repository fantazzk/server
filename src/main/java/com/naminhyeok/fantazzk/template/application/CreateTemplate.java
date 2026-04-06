package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.repository.Templates;
import org.springframework.transaction.annotation.Transactional;

@org.jmolecules.ddd.annotation.Service
@org.springframework.stereotype.Service
public class CreateTemplate {
    private final Templates templateRepository;

    public CreateTemplate(Templates templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional
    public Template create(CreateTemplateCommand command) {
        Template template = switch (command) {
            case CreateTemplateCommand.Auction auction -> Template.createAuction(
                auction.getName(),
                auction.getTeamCount(),
                auction.getTeamSize(),
                auction.getBudget(),
                auction.getPlayerNames()
            );
            case CreateTemplateCommand.Draft draft -> Template.createDraft(
                draft.getName(),
                draft.getTeamCount(),
                draft.getTeamSize(),
                draft.getStrategy(),
                draft.getPlayerNames()
            );
        };
        return templateRepository.save(template);
    }
}
