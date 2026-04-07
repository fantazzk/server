package com.naminhyeok.fantazzk.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ProvideTemplateCatalog implements TemplateCatalog {
    private final FindTemplates findTemplates;

    @Override
    public TemplateBlueprint getTemplate(TemplateId templateId) {
        try {
            TemplateDetail detail = findTemplates.getDetail(templateId);
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
        } catch (TemplateException ex) {
            if (ex.getError() == TemplateErrorType.TEMPLATE_NOT_FOUND) {
                throw new TemplateCatalogException.NotFound(templateId);
            }
            throw ex;
        }
    }
}
