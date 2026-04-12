package com.naminhyeok.fantazzk.template;

import java.util.Set;

enum GameType {
    LEAGUE_OF_LEGENDS(Set.of("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")),
    OVERWATCH_2(Set.of("TANK", "DPS", "SUPPORT"));

    private final Set<String> supportedPositions;

    GameType(Set<String> supportedPositions) {
        this.supportedPositions = supportedPositions;
    }

    boolean supportsPosition(String position) {
        return supportedPositions.contains(normalize(position));
    }

    void validatePosition(String position) {
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
