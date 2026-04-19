package com.naminhyeok.fantazzk.template.query;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;

public final class TemplateExternalViewMapper {
    private TemplateExternalViewMapper() {
    }

    public static TemplateCatalog.TemplateBlueprint toBlueprint(TemplateDetail detail) {
        Template template = detail.template();
        return new TemplateCatalog.TemplateBlueprint(
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getPickBanTime(),
            template.getMinBidUnit(),
            template.getPositionLimit(),
            template.getDraftOrderStrategy(),
            detail.players().stream()
                .map(player -> new TemplateCatalog.PlayerBlueprint(player.name(), player.position(), player.displayOrder()))
                .toList()
        );
    }

    public static TemplateSummaryResponse toSummaryResponse(Template template) {
        return new TemplateSummaryResponse(
            template.getId().templateId().toString(),
            template.getName(),
            template.getGameType(),
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize()
        );
    }

    public static TemplateDetailResponse toDetailResponse(TemplateDetail detail) {
        Template template = detail.template();
        return new TemplateDetailResponse(
            template.getId().templateId().toString(),
            template.getName(),
            template.getGameType(),
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getPickBanTime(),
            template.getMinBidUnit(),
            template.getPositionLimit(),
            template.getDraftOrderStrategy(),
            detail.players().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
