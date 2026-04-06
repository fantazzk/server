package com.naminhyeok.fantazzk.template;

import java.util.Objects;

public final class TemplatePlayerBlueprint {
    private final String name;
    private final int displayOrder;

    public TemplatePlayerBlueprint(String name, int displayOrder) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
