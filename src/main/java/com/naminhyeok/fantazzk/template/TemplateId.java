package com.naminhyeok.fantazzk.template;

import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class TemplateId implements Identifier {
    private final UUID value;

    public TemplateId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public TemplateId(String value) {
        this(UUID.fromString(value));
    }

    public static TemplateId from(UUID value) {
        return new TemplateId(value);
    }

    public static TemplateId from(String value) {
        return new TemplateId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TemplateId other)) {
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
