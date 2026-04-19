package com.naminhyeok.fantazzk.template;

record TemplateSummaryResponse(
    String id,
    String name,
    TemplateCatalog.GameType gameType,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize
) {
    static TemplateSummaryResponse from(Template template) {
        return TemplateExternalViewMapper.toSummaryResponse(template);
    }
}
