package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.TemplateErrorType;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
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
            TemplateDetail detail = findTemplates.getDetail(new TemplateId(templateId));
            return new TemplateBlueprint(
                templateId,
                detail.template().getMode() == com.naminhyeok.fantazzk.template.domain.TemplateMode.AUCTION
                    ? Mode.AUCTION
                    : Mode.DRAFT,
                detail.template().getTeamCount(),
                detail.template().getTeamSize(),
                detail.template().getBudget(),
                detail.template().getDraftOrderStrategy() == null
                    ? null
                    : DraftOrderStrategy.valueOf(detail.template().getDraftOrderStrategy().name()),
                detail.players().stream()
                    .map(player -> new PlayerBlueprint(player.getName(), player.getDisplayOrder()))
                    .toList()
            );
        } catch (CoreException ex) {
            if (ex.getError() == TemplateErrorType.TEMPLATE_NOT_FOUND) {
                throw new TemplateCatalog.NotFound(templateId);
            }
            throw ex;
        }
    }
}
