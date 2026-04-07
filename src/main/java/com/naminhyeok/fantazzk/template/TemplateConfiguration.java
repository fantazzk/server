package com.naminhyeok.fantazzk.template;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.ValueObject;

@Getter
@EqualsAndHashCode
public final class TemplateConfiguration implements ValueObject {
    @Enumerated(EnumType.STRING)
    private final TemplateMode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    @Enumerated(EnumType.STRING)
    private final DraftOrderStrategy draftOrderStrategy;

    private TemplateConfiguration(
        TemplateMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        DraftOrderStrategy draftOrderStrategy
    ) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }

        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }

        if (mode == TemplateMode.AUCTION) {
            if (budget == null) {
                throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
            }
            if (budget <= 0) {
                throw new IllegalArgumentException("예산은 0보다 커야 합니다");
            }
            if (draftOrderStrategy != null) {
                throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
            }
        }

        if (mode == TemplateMode.DRAFT) {
            if (budget != null) {
                throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
            }
            if (draftOrderStrategy == null) {
                throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
            }
        }

        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
    }

    public static TemplateConfiguration auction(int teamCount, int teamSize, int budget) {
        return new TemplateConfiguration(TemplateMode.AUCTION, teamCount, teamSize, budget, null);
    }

    public static TemplateConfiguration draft(int teamCount, int teamSize, DraftOrderStrategy strategy) {
        return new TemplateConfiguration(TemplateMode.DRAFT, teamCount, teamSize, null, strategy);
    }

    public static TemplateConfiguration from(
        TemplateMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
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

    public int requiredPlayerCount() {
        return teamCount * (teamSize - 1);
    }
}
