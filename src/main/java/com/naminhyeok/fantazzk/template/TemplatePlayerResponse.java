package com.naminhyeok.fantazzk.template;

record TemplatePlayerResponse(
    String name,
    String position,
    int displayOrder
) {
    static TemplatePlayerResponse from(TemplatePlayer player) {
        return new TemplatePlayerResponse(player.name(), player.position(), player.displayOrder());
    }
}
