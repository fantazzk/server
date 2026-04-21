package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateErrorType;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import com.naminhyeok.fantazzk.template.query.FindTemplates;
import com.naminhyeok.fantazzk.template.query.TemplateDetail;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProvideTemplateCatalog implements TemplateCatalog {
    private final FindTemplates findTemplates;

    @Override
    public TemplateBlueprint getTemplate(UUID templateId) {
        try {
            TemplateDetail detail = findTemplates.getDetail(new TemplateId(templateId));
            Template template = detail.template();
            return new TemplateBlueprint(
                template.getGameType(),
                template.getMode(),
                template.getTeamCount(),
                template.getTeamSize(),
                template.getBudget(),
                template.getPickBanTime(),
                template.getMinBidUnit(),
                template.getDraftOrderStrategy(),
                detail.players().stream()
                    .map(player -> new PlayerBlueprint(player.name(), player.position(), player.displayOrder()))
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
