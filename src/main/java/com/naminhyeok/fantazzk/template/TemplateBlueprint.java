package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public final class TemplateBlueprint {
    private final TemplateId templateId;
    private final TemplateMode mode;
    private final int teamCount;
    private final int teamSize;
    @Nullable
    private final Integer budget;
    @Nullable
    private final TemplateDraftOrderStrategy draftOrderStrategy;
    private final List<TemplatePlayerBlueprint> players;

    public TemplateBlueprint(
        TemplateId templateId,
        TemplateMode mode,
        int teamCount,
        int teamSize,
        @Nullable
        Integer budget,
        @Nullable
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

    @Nullable
    public Integer getBudget() {
        return budget;
    }

    @Nullable
    public TemplateDraftOrderStrategy getDraftOrderStrategy() {
        return draftOrderStrategy;
    }

    public List<TemplatePlayerBlueprint> getPlayers() {
        return players;
    }
}
