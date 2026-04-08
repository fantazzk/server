package com.naminhyeok.fantazzk.template.api;

import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;

public record TemplatePlayerResponse(
    String name,
    int displayOrder
) {
    static TemplatePlayerResponse from(TemplatePlayer player) {
        return new TemplatePlayerResponse(player.getName(), player.getDisplayOrder());
    }
}
