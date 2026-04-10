package com.naminhyeok.fantazzk.template;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import org.jmolecules.ddd.types.ValueObject;

@Embeddable
@EqualsAndHashCode
final class TemplatePlayer implements ValueObject {
    private final String name;
    @Column(name = "display_order")
    private final int playerIndex;

    TemplatePlayer(String name, int playerIndex) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("선수 이름은 비어 있을 수 없습니다");
        }

        this.name = name;
        this.playerIndex = playerIndex;
    }

    String name() {
        return name;
    }

    int playerIndex() {
        return playerIndex;
    }
}
