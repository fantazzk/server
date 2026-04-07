package com.naminhyeok.fantazzk.template;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ProvideTemplateCatalog implements TemplateCatalog {
    private final FindTemplates findTemplates;

    @Override
    public TemplateBlueprint getTemplate(UUID templateId) {
        try {
            TemplateDetail detail = findTemplates.getDetail(new Template.TemplateId(templateId));
            return new TemplateBlueprint(
                templateId,
                detail.template().getMode(),
                detail.template().getTeamCount(),
                detail.template().getTeamSize(),
                detail.template().getBudget(),
                detail.template().getDraftOrderStrategy(),
                detail.players().stream()
                    .map(player -> new TemplatePlayerBlueprint(player.getName(), player.getDisplayOrder()))
                    .toList()
            );
        } catch (TemplateNotFoundException ex) {
            throw new TemplateCatalogException.NotFound(templateId);
        }
    }
}
