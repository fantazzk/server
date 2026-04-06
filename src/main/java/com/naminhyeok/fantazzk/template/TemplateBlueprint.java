package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.Objects;

public final class TemplateBlueprint {

    private final TemplateId templateId;
    private final TemplateMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    private final TemplateDraftOrderStrategy draftOrderStrategy;
    private final List<TemplatePlayerBlueprint> players;

    public TemplateBlueprint(
        TemplateId templateId,
        TemplateMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        TemplateDraftOrderStrategy draftOrderStrategy,
        List<TemplatePlayerBlueprint> players
    ) {
        this.templateId = Objects.requireNonNull(templateId, "templateId must not be null");
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        this.players = List.copyOf(Objects.requireNonNull(players, "players must not be null"));
    }

    public TemplateId getTemplateId() {
        return templateId;
    }

    public TemplateMode getMode() {
        return mode;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public Integer getBudget() {
        return budget;
    }

    public TemplateDraftOrderStrategy getDraftOrderStrategy() {
        return draftOrderStrategy;
    }

    public List<TemplatePlayerBlueprint> getPlayers() {
        return players;
    }
}
