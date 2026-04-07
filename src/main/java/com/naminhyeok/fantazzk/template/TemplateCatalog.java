package com.naminhyeok.fantazzk.template;

import java.util.List;

public interface TemplateCatalog {
    TemplateBlueprint getTemplate(TemplateId templateId);

    record TemplateBlueprint(
        TemplateId templateId,
        TemplateMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        DraftOrderStrategy draftOrderStrategy,
        List<TemplatePlayerBlueprint> players
    ) {
    }

    record TemplatePlayerBlueprint(
        String name,
        int displayOrder
    ) {
    }
}
