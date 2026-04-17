package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.CoreException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProvideTemplateCatalog implements TemplateCatalog {
    private final FindTemplates findTemplates;

    @Override
    public TemplateBlueprint getTemplate(UUID templateId) {
        try {
            TemplateDetail detail = findTemplates.getDetail(new TemplateId(templateId));
            return new TemplateBlueprint(
                templateId,
                GameType.valueOf(detail.template().getGameType().name()),
                detail.template().getMode() == TemplateMode.AUCTION
                    ? Mode.AUCTION
                    : Mode.DRAFT,
                detail.template().getTeamCount(),
                detail.template().getTeamSize(),
                detail.template().getBudget(),
                detail.template().getPickBanTime(),
                detail.template().getMinBidUnit(),
                detail.template().getPositionLimit(),
                detail.template().getDraftOrderStrategy() == null
                    ? null
                    : DraftOrderStrategy.valueOf(detail.template().getDraftOrderStrategy().name()),
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
