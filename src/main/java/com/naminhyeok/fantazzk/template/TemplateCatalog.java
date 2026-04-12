package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.UUID;

public interface TemplateCatalog {
    TemplateBlueprint getTemplate(UUID templateId);

    enum GameType {
        LEAGUE_OF_LEGENDS,
        OVERWATCH_2
    }

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
        GameType gameType,
        Mode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        Integer pickBanTime,
        Integer minBidUnit,
        Integer positionLimit,
        DraftOrderStrategy draftOrderStrategy,
        List<PlayerBlueprint> players
    ) {
    }

    record PlayerBlueprint(
        String name,
        String position,
        int playerIndex
    ) {
    }

    final class NotFound extends RuntimeException {
        public NotFound(UUID templateId) {
            super("템플릿을 찾을 수 없습니다: " + templateId);
        }
    }
}
