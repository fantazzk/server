package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import java.util.List;
import java.util.Objects;

public final class TemplateDetail {
    private final Template template;
    private final List<TemplatePlayer> players;

    public TemplateDetail(Template template, List<TemplatePlayer> players) {
        this.template = Objects.requireNonNull(template, "template must not be null");
        this.players = List.copyOf(Objects.requireNonNull(players, "players must not be null"));
    }

    public Template getTemplate() {
        return template;
    }

    public List<TemplatePlayer> getPlayers() {
        return players;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TemplateDetail that)) {
            return false;
        }
        return template.equals(that.template) && players.equals(that.players);
    }

    @Override
    public int hashCode() {
        return 31 * template.hashCode() + players.hashCode();
    }

    @Override
    public String toString() {
        return "TemplateDetail(template=" + template + ", players=" + players + ")";
    }
}
