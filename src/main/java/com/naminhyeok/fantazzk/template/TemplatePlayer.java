package com.naminhyeok.fantazzk.template;

import java.util.UUID;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

class TemplatePlayer implements Entity<Template, TemplatePlayer.TemplatePlayerId> {
    private final TemplatePlayerId id;
    private String name;
    private int displayOrder;

    TemplatePlayer(String name, int displayOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("선수 이름은 비어 있을 수 없습니다");
        }

        this.id = new TemplatePlayerId(UUID.randomUUID());
        this.name = name;
        this.displayOrder = displayOrder;
    }

    @Override
    public TemplatePlayerId getId() {
        return id;
    }

    String getName() {
        return name;
    }

    int getDisplayOrder() {
        return displayOrder;
    }

    record TemplatePlayerId(UUID templatePlayerId) implements Identifier {
    }
}
