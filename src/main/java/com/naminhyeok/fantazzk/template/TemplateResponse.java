package com.naminhyeok.fantazzk.template;

import java.util.List;

record TemplateResponse(
    String id,
    String name,
    TemplateCatalog.GameType gameType,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    Integer pickBanTime,
    Integer minBidUnit,
    Integer positionLimit,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    List<TemplatePlayerResponse> players
) {
    static TemplateResponse from(Template template) {
        return from(new TemplateDetail(template, template.getPlayers()));
    }

    static TemplateResponse from(TemplateDetail detail) {
        return new TemplateResponse(
            detail.template().getId().templateId().toString(),
            detail.template().getName(),
            TemplateCatalog.GameType.valueOf(detail.template().getGameType().name()),
            detail.template().getMode() == TemplateMode.AUCTION ? TemplateCatalog.Mode.AUCTION : TemplateCatalog.Mode.DRAFT,
            detail.template().getTeamCount(),
            detail.template().getTeamSize(),
            detail.template().getBudget(),
            detail.template().getPickBanTime(),
            detail.template().getMinBidUnit(),
            detail.template().getPositionLimit(),
            detail.template().getDraftOrderStrategy() == null
                ? null
                : TemplateCatalog.DraftOrderStrategy.valueOf(detail.template().getDraftOrderStrategy().name()),
            detail.players().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
