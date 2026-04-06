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
    @Nullable
    private Integer budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "draft_order_strategy")
    @Nullable
    private DraftOrderStrategy draftOrderStrategy;

    protected TemplateConfiguration() {
    }

    private TemplateConfiguration(
        TeamBuildingMode mode,
        int teamCount,
        int teamSize,
        @Nullable
        Integer budget,
        @Nullable
        DraftOrderStrategy draftOrderStrategy
    ) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        validate();
    }

    public static TemplateConfiguration auction(int teamCount, int teamSize, int budget) {
        return new TemplateConfiguration(TeamBuildingMode.AUCTION, teamCount, teamSize, budget, null);
    }

    public static TemplateConfiguration draft(int teamCount, int teamSize, DraftOrderStrategy strategy) {
        return new TemplateConfiguration(TeamBuildingMode.DRAFT, teamCount, teamSize, null, strategy);
    }

    public static TemplateConfiguration from(
        TeamBuildingMode mode,
        int teamCount,
        int teamSize,
        @Nullable
        Integer budget,
        @Nullable
        DraftOrderStrategy draftOrderStrategy
    ) {
        return switch (mode) {
            case AUCTION -> {
                if (draftOrderStrategy != null) {
                    throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
                }
                if (budget == null) {
                    throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
                }
                yield auction(teamCount, teamSize, budget);
            }
            case DRAFT -> {
                if (budget != null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
                }
                if (draftOrderStrategy == null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
                }
                yield draft(teamCount, teamSize, draftOrderStrategy);
            }
        };
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

    private void validate() {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }
        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }
        switch (mode) {
            case AUCTION -> {
                int currentBudget = Objects.requireNonNull(budget, "경매 템플릿에는 예산이 필요합니다");
                if (currentBudget <= 0) {
                    throw new IllegalArgumentException("예산은 0보다 커야 합니다");
                }
                if (draftOrderStrategy != null) {
                    throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
                }
            }
            case DRAFT -> {
                if (budget != null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
                }
                Objects.requireNonNull(draftOrderStrategy, "드래프트 템플릿에는 순서 전략이 필요합니다");
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TemplateConfiguration other)) {
            return false;
        }
        return teamCount == other.teamCount
            && teamSize == other.teamSize
            && mode == other.mode
            && Objects.equals(budget, other.budget)
            && draftOrderStrategy == other.draftOrderStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, teamCount, teamSize, budget, draftOrderStrategy);
    }

    @Override
    public String toString() {
        return "TemplateConfiguration(" +
            "mode=" + mode +
            ", teamCount=" + teamCount +
            ", teamSize=" + teamSize +
            ", budget=" + budget +
            ", draftOrderStrategy=" + draftOrderStrategy +
            ')';
    }
}
