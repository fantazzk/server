package com.naminhyeok.fantazzk.template.web;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.TemplateDetailView;
import com.naminhyeok.fantazzk.template.TemplateSummaryView;
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
    static TemplateResponse from(TemplateSummaryView template) {
        return new TemplateResponse(
            template.id(),
            template.name(),
            template.mode(),
            template.teamCount(),
            template.teamSize(),
            template.budget(),
            template.draftOrderStrategy(),
            null
        );
    }

    static TemplateResponse from(TemplateDetailView detail) {
        return new TemplateResponse(
            detail.id(),
            detail.name(),
            detail.mode(),
            detail.teamCount(),
            detail.teamSize(),
            detail.budget(),
            detail.draftOrderStrategy(),
            detail.players().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
