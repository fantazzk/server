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
        return TemplateExternalViewMapper.toResponse(detail);
    }
}
