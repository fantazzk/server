package com.naminhyeok.fantazzk.template;

public record TemplatePlayerResponse(
    String name,
    int displayOrder
) {
    static TemplatePlayerResponse from(TemplatePlayer player) {
        return new TemplatePlayerResponse(player.getName(), player.getDisplayOrder());
    }
}
