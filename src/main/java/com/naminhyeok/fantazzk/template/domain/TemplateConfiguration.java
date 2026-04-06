package com.naminhyeok.fantazzk.template.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

@Embeddable
public class TemplateConfiguration {
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private TeamBuildingMode mode;

    @Column(name = "team_count", nullable = false)
    private int teamCount;

    @Column(name = "team_size", nullable = false)
    private int teamSize;

    @Column(name = "budget")
    private Integer budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_order_strategy")
    private DraftOrderStrategy draftOrderStrategy;

    protected TemplateConfiguration() {
        this(TeamBuildingMode.AUCTION, 1, 2, 1, null);
    }

    protected TemplateConfiguration(
        TeamBuildingMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        DraftOrderStrategy draftOrderStrategy
    ) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        validate();
    }

    public TeamBuildingMode getMode() {
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
    public DraftOrderStrategy getDraftOrderStrategy() {
        return draftOrderStrategy;
    }

    public int getRequiredPlayerCount() {
        return teamCount * (teamSize - 1);
    }

    public static TemplateConfiguration auction(
        int teamCount,
        int teamSize,
        int budget
    ) {
        return new TemplateConfiguration(
            TeamBuildingMode.AUCTION,
            teamCount,
            teamSize,
            budget,
            null
        );
    }

    public static TemplateConfiguration draft(
        int teamCount,
        int teamSize,
        DraftOrderStrategy strategy
    ) {
        return new TemplateConfiguration(
            TeamBuildingMode.DRAFT,
            teamCount,
            teamSize,
            null,
            strategy
        );
    }

    public static TemplateConfiguration from(
        TeamBuildingMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        DraftOrderStrategy draftOrderStrategy
    ) {
        if (mode == TeamBuildingMode.AUCTION) {
            if (draftOrderStrategy != null) {
                throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
            }
            if (budget == null) {
                throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
            }
            return auction(teamCount, teamSize, budget);
        }

        if (budget != null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
        }
        if (draftOrderStrategy == null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
        }
        return draft(
            teamCount,
            teamSize,
            draftOrderStrategy
        );
    }

    private void validate() {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }
        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }

        if (mode == TeamBuildingMode.AUCTION) {
            if (budget == null) {
                throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
            }
            if (budget <= 0) {
                throw new IllegalArgumentException("예산은 0보다 커야 합니다");
            }
            if (draftOrderStrategy != null) {
                throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
            }
            return;
        }

        if (budget != null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
        }
        if (draftOrderStrategy == null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TemplateConfiguration that)) {
            return false;
        }
        return teamCount == that.teamCount
            && teamSize == that.teamSize
            && Objects.equals(mode, that.mode)
            && Objects.equals(budget, that.budget)
            && draftOrderStrategy == that.draftOrderStrategy;
    }

    @Override
    public int hashCode() {
        int result = mode.hashCode();
        result = 31 * result + teamCount;
        result = 31 * result + teamSize;
        result = 31 * result + (budget == null ? 0 : budget);
        result = 31 * result + (draftOrderStrategy == null ? 0 : draftOrderStrategy.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TemplateConfiguration("
            + "mode="
            + mode
            + ", teamCount="
            + teamCount
            + ", teamSize="
            + teamSize
            + ", budget="
            + budget
            + ", draftOrderStrategy="
            + draftOrderStrategy
            + ")";
    }
}
