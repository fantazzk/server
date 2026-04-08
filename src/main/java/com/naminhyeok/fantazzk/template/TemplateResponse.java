package com.naminhyeok.fantazzk.template;
import java.util.List;

record TemplateResponse(
    String id,
    String name,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    List<TemplatePlayerResponse> players
) {
    static TemplateResponse from(Template template) {
        return new TemplateResponse(
            template.getId().templateId().toString(),
            template.getName(),
            template.getMode() == TemplateMode.AUCTION ? TemplateCatalog.Mode.AUCTION : TemplateCatalog.Mode.DRAFT,
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getDraftOrderStrategy() == null ? null : TemplateCatalog.DraftOrderStrategy.valueOf(template.getDraftOrderStrategy().name()),
            null
        );
    }

    static TemplateResponse from(TemplateDetail detail) {
        return new TemplateResponse(
            detail.template().getId().templateId().toString(),
            detail.template().getName(),
            detail.template().getMode() == TemplateMode.AUCTION ? TemplateCatalog.Mode.AUCTION : TemplateCatalog.Mode.DRAFT,
            detail.template().getTeamCount(),
            detail.template().getTeamSize(),
            detail.template().getBudget(),
            detail.template().getDraftOrderStrategy() == null
                ? null
                : TemplateCatalog.DraftOrderStrategy.valueOf(detail.template().getDraftOrderStrategy().name()),
            detail.players().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
