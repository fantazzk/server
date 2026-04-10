package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.UUID;

public interface TemplateCatalog {
    TemplateBlueprint getTemplate(UUID templateId);

    enum Mode {
        AUCTION,
        DRAFT
    }

    enum DraftOrderStrategy {
        SNAKE,
        FIXED
    }

    record TemplateBlueprint(
        UUID templateId,
        Mode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        DraftOrderStrategy draftOrderStrategy,
        List<PlayerBlueprint> players
    ) {
    }

    record PlayerBlueprint(
        String name,
        int playerIndex
    ) {
    }

    final class NotFound extends RuntimeException {
        public NotFound(UUID templateId) {
            super("템플릿을 찾을 수 없습니다: " + templateId);
        }
    }
}
