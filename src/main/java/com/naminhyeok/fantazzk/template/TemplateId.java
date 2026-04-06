package com.naminhyeok.fantazzk.template;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

@Embeddable
public final class TemplateId implements Identifier, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    protected TemplateId() {}

    public TemplateId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
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
