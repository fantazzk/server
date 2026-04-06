package com.naminhyeok.fantazzk.template.domain;

import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class TemplatePlayerId implements Identifier {
    private final UUID value;

    public TemplatePlayerId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public TemplatePlayerId(String value) {
        this(UUID.fromString(value));
    }

    public static TemplatePlayerId from(UUID value) {
        return new TemplatePlayerId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TemplatePlayerId other)) {
            return false;
        }
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
