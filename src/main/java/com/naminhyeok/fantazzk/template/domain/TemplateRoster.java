package com.naminhyeok.fantazzk.template.domain;

import java.util.List;
import java.util.Objects;

public final class TemplateRoster {
    private final List<String> names;

    private TemplateRoster(List<String> names) {
        this.names = List.copyOf(names);
    }

    public List<String> playerNames() {
        return names;
    }

    public static TemplateRoster exactlyRequired(List<String> playerNames, int requiredPlayerCount) {
        Objects.requireNonNull(playerNames, "playerNames must not be null");
        if (playerNames.size() != requiredPlayerCount) {
            throw new IllegalArgumentException("선수 수는 정확히 %d명이어야 합니다".formatted(requiredPlayerCount));
        }
        return new TemplateRoster(List.copyOf(playerNames));
    }
}
