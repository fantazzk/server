package com.naminhyeok.fantazzk.template;

import java.util.List;

public record TemplateResponse(
    String id,
    String name,
    TemplateMode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    DraftOrderStrategy draftOrderStrategy,
    List<TemplatePlayerResponse> players
) {
    static TemplateResponse from(Template template) {
        return new TemplateResponse(
            template.getId().templateId().toString(),
            template.getName(),
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getDraftOrderStrategy(),
            null
        );
    }

    static TemplateResponse from(TemplateDetail detail) {
        return new TemplateResponse(
            detail.template().getId().templateId().toString(),
            detail.template().getName(),
            detail.template().getMode(),
            detail.template().getTeamCount(),
            detail.template().getTeamSize(),
            detail.template().getBudget(),
            detail.template().getDraftOrderStrategy(),
            detail.players().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
