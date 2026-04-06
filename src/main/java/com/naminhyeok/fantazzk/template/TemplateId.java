package com.naminhyeok.fantazzk.template;

import java.util.Objects;
import java.util.UUID;

import org.jmolecules.ddd.types.Identifier;

public final class TemplateId implements Identifier {

    private final UUID value;

    public TemplateId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public TemplateId(long value) {
        this(new UUID(0L, value));
    }

    public UUID getValue() {
        return value;
    }

    public static TemplateId of(String value) {
        return new TemplateId(UUID.fromString(value));
    }

    public static TemplateId of(UUID value) {
        return new TemplateId(value);
    }

    public static TemplateId of(long value) {
        return new TemplateId(value);
    }

    public static TemplateId newId() {
        return new TemplateId(UUID.randomUUID());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof TemplateId that)) {
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
        return "TemplateId[value=%s]".formatted(value);
    }
}
