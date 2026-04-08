package com.naminhyeok.fantazzk.template;

import java.util.List;

public record CreateTemplateInput(
    String name,
    TemplateCatalog.Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    List<String> playerNames
) {
}
