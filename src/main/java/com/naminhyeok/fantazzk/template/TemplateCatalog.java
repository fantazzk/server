package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.UUID;

public interface TemplateCatalog {
    TemplateBlueprint getTemplate(UUID templateId);

    record TemplateBlueprint(
        UUID templateId,
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
