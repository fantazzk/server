package com.naminhyeok.fantazzk.template.domain;

import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class TemplatePlayerId implements Identifier {

    private final UUID value;

    public TemplatePlayerId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public UUID getValue() {
        return value;
    }

    public static TemplatePlayerId of(String value) {
        return new TemplatePlayerId(UUID.fromString(value));
    }

    public static TemplatePlayerId of(UUID value) {
        return new TemplatePlayerId(value);
    }

    public static TemplatePlayerId newId() {
        return new TemplatePlayerId(UUID.randomUUID());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TemplatePlayerId that)) {
            return false;
        }
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "TemplatePlayerId[value=%s]".formatted(value);
    }
}
