package com.naminhyeok.fantazzk.template;

public record TemplateSummaryView(
    String id,
    String name,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy
) {
    static TemplateSummaryView from(Template template) {
        return new TemplateSummaryView(
            template.getId().templateId().toString(),
            template.getName(),
            template.getMode() == TemplateMode.AUCTION ? TemplateCatalog.Mode.AUCTION : TemplateCatalog.Mode.DRAFT,
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getDraftOrderStrategy() == null
                ? null
                : TemplateCatalog.DraftOrderStrategy.valueOf(template.getDraftOrderStrategy().name())
        );
    }
}
