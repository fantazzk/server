package com.naminhyeok.fantazzk.template;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import org.jmolecules.ddd.types.ValueObject;

@Embeddable
@EqualsAndHashCode
final class TemplatePlayer implements ValueObject {
    private final String name;
    private final String position;
    @Column(name = "display_order")
    private final int displayOrder;

    TemplatePlayer(String name, String position, int displayOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("선수 이름은 비어 있을 수 없습니다");
        }
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("선수 포지션은 비어 있을 수 없습니다");
        }

        this.name = name.trim();
        this.position = position.trim().toUpperCase();
        this.displayOrder = displayOrder;
    }

    @SuppressWarnings("unused")
    private TemplatePlayer() {
        this.name = null;
        this.position = null;
        this.displayOrder = 0;
    }

    String name() {
        return name;
    }

    String position() {
        return position;
    }

    int displayOrder() {
        return displayOrder;
    }

    public String getName() {
        return name();
    }
}
