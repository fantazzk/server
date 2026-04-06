package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.TemplateBlueprint;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.TemplateCatalogException;
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy;
import com.naminhyeok.fantazzk.template.TemplateId;
import com.naminhyeok.fantazzk.template.TemplateMode;
import com.naminhyeok.fantazzk.template.TemplatePlayerBlueprint;
import com.naminhyeok.fantazzk.template.exception.TemplateException;
import java.util.List;
import java.util.stream.Collectors;

class ProvideTemplateCatalog implements TemplateCatalog {
    private final FindTemplates templateFinder;

    ProvideTemplateCatalog(FindTemplates templateFinder) {
        this.templateFinder = templateFinder;
    }

    @Override
    public TemplateBlueprint getTemplateBlueprint(TemplateId templateId) {
        try {
            TemplateDetail detail = templateFinder.getDetail(templateId);
            List<TemplatePlayerBlueprint> players =
                detail
                    .getPlayers()
                    .stream()
                    .map(player -> new TemplatePlayerBlueprint(player.getName(), player.getDisplayOrder()))
                    .collect(Collectors.toList());
            return new TemplateBlueprint(
                templateId,
                TemplateMode.valueOf(detail.getTemplate().getMode().name()),
                detail.getTemplate().getTeamCount(),
                detail.getTemplate().getTeamSize(),
                detail.getTemplate().getBudget(),
                detail.getTemplate().getDraftOrderStrategy() != null
                    ? TemplateDraftOrderStrategy.valueOf(detail.getTemplate().getDraftOrderStrategy().name())
                    : null,
                players
            );
        } catch (TemplateException.TemplateNotFoundException e) {
            throw new TemplateCatalogException.NotFound(templateId);
        } catch (TemplateException.TemplateInvalidException e) {
            throw new TemplateCatalogException.Invalid(templateId);
        }
    }
}
