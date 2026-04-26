package com.naminhyeok.fantazzk.template.domain;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.ValueObject;

@Getter
@EqualsAndHashCode
public final class TemplateConfiguration implements ValueObject {
    @Enumerated(EnumType.STRING)
    private final TemplateCatalog.Mode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    private final int pickBanTime;
    private final Integer minBidUnit;
    @Enumerated(EnumType.STRING)
    private final TemplateCatalog.DraftOrderStrategy draftOrderStrategy;

    private TemplateConfiguration(
        TemplateCatalog.Mode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        Integer minBidUnit,
        TemplateCatalog.DraftOrderStrategy draftOrderStrategy
    ) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }
        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }
        if (pickBanTime <= 0) {
            throw new IllegalArgumentException("픽밴 시간은 0보다 커야 합니다");
        }

        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.pickBanTime = pickBanTime;
        this.minBidUnit = minBidUnit;
        this.draftOrderStrategy = draftOrderStrategy;
    }

    public static TemplateConfiguration auction(
        int teamCount,
        int teamSize,
        int budget,
        int pickBanTime,
        int minBidUnit
    ) {
        if (budget <= 0) {
            throw new IllegalArgumentException("예산은 0보다 커야 합니다");
        }
        if (minBidUnit <= 0) {
            throw new IllegalArgumentException("최소 입찰 단위는 0보다 커야 합니다");
        }
        return new TemplateConfiguration(
            TemplateCatalog.Mode.AUCTION,
            teamCount,
            teamSize,
            budget,
            pickBanTime,
            minBidUnit,
            null
        );
    }

    public static TemplateConfiguration draft(
        int teamCount,
        int teamSize,
        int pickBanTime,
        TemplateCatalog.DraftOrderStrategy strategy
    ) {
        if (strategy == null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
        }
        return new TemplateConfiguration(
            TemplateCatalog.Mode.DRAFT,
            teamCount,
            teamSize,
            null,
            pickBanTime,
            null,
            strategy
        );
    }

    public int requiredPlayerCount() {
        return teamCount * (teamSize - 1);
    }
}
