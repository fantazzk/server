package com.naminhyeok.fantazzk.template.query;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;

public record TemplateSummaryResponse(
    String id,
    String name,
    TemplateCatalog.GameType gameType,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize
) {
    public static TemplateSummaryResponse from(Template template) {
        return TemplateExternalViewMapper.toSummaryResponse(template);
    }
}
