package com.naminhyeok.fantazzk.template.web;

import com.naminhyeok.fantazzk.template.TemplatePlayerView;

record TemplatePlayerResponse(
    String name,
    int displayOrder
) {
    static TemplatePlayerResponse from(TemplatePlayerView player) {
        return new TemplatePlayerResponse(player.name(), player.displayOrder());
    }
}
