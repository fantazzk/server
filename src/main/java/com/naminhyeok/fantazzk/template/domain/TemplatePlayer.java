package com.naminhyeok.fantazzk.template.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import org.jmolecules.ddd.types.ValueObject;

@Embeddable
@EqualsAndHashCode
public final class TemplatePlayer implements ValueObject {
    private final String name;
    private final String position;
    @Column(name = "display_order")
    private final int displayOrder;

    public TemplatePlayer(String name, String position, int displayOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("선수 이름은 비어 있을 수 없습니다");
        }

        this.name = name.trim();
        this.position = position;
        this.displayOrder = displayOrder;
    }

    @SuppressWarnings("unused")
    private TemplatePlayer() {
        this.name = null;
        this.position = null;
        this.displayOrder = 0;
    }

    public String name() {
        return name;
    }

    public String position() {
        return position;
    }

    public int displayOrder() {
        return displayOrder;
    }

    public String getName() {
        return name();
    }
}
