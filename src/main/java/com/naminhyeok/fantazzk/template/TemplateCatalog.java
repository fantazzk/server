package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TemplateCatalog {
    TemplateBlueprint getTemplate(UUID templateId);

    enum GameType {
        LEAGUE_OF_LEGENDS(Set.of("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")),
        OVERWATCH_2(Set.of("TANK", "DPS", "SUPPORT"));

        private final Set<String> supportedPositions;

        GameType(Set<String> supportedPositions) {
            this.supportedPositions = supportedPositions;
        }

        public boolean supportsPosition(String position) {
            return supportedPositions.contains(normalize(position));
        }

        public void validatePosition(String position) {
            String normalizedPosition = normalize(position);

            if (!supportsPosition(normalizedPosition)) {
                throw new IllegalArgumentException(name() + " 게임은 " + normalizedPosition + " 포지션을 지원하지 않습니다");
            }
        }

        private static String normalize(String position) {
            if (position == null || position.isBlank()) {
                throw new IllegalArgumentException("포지션은 비어 있을 수 없습니다");
            }

            return position.trim().toUpperCase();
        }
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
