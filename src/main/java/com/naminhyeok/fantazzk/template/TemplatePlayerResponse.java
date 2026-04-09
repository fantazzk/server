package com.naminhyeok.fantazzk.template;

record TemplatePlayerResponse(
    String name,
    int displayOrder
) {
    static TemplatePlayerResponse from(TemplatePlayer player) {
        return new TemplatePlayerResponse(player.getName(), player.getPlayerIndex());
    }
}
