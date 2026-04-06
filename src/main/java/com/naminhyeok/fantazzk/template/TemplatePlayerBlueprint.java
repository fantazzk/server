package com.naminhyeok.fantazzk.template;

public final class TemplatePlayerBlueprint {

    private final String name;
    private final int displayOrder;

    public TemplatePlayerBlueprint(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
