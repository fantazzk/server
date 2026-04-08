package com.naminhyeok.fantazzk.template;

import java.util.List;

public record TemplateDetailView(
    String id,
    String name,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    List<TemplatePlayerView> players
) {
    static TemplateDetailView from(TemplateDetail detail) {
        return new TemplateDetailView(
            detail.template().getId().templateId().toString(),
            detail.template().getName(),
            detail.template().getMode() == TemplateMode.AUCTION ? TemplateCatalog.Mode.AUCTION : TemplateCatalog.Mode.DRAFT,
            detail.template().getTeamCount(),
            detail.template().getTeamSize(),
            detail.template().getBudget(),
            detail.template().getDraftOrderStrategy() == null
                ? null
                : TemplateCatalog.DraftOrderStrategy.valueOf(detail.template().getDraftOrderStrategy().name()),
            detail.players().stream().map(TemplatePlayerView::from).toList()
        );
    }
}
