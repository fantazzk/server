package com.naminhyeok.fantazzk.template;

public record TemplatePlayerView(
    String name,
    int displayOrder
) {
    static TemplatePlayerView from(TemplatePlayer player) {
        return new TemplatePlayerView(player.getName(), player.getDisplayOrder());
    }
}
