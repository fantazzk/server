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
    private final TemplateCatalog.GameType gameType;
    @Enumerated(EnumType.STRING)
    private final TemplateCatalog.Mode mode;
    private final int teamCount;
    private final int teamSize;
    private final Integer budget;
    private final int pickBanTime;
    private final Integer minBidUnit;
    private final Integer positionLimit;
    @Enumerated(EnumType.STRING)
    private final TemplateCatalog.DraftOrderStrategy draftOrderStrategy;

    private TemplateConfiguration(
        TemplateCatalog.GameType gameType,
        TemplateCatalog.Mode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        Integer minBidUnit,
        Integer positionLimit,
        TemplateCatalog.DraftOrderStrategy draftOrderStrategy
    ) {
        if (gameType == null) {
            throw new IllegalArgumentException("게임 타입은 필수입니다");
        }
        if (teamCount <= 0) {
            throw new IllegalArgumentException("팀 수는 0보다 커야 합니다");
        }
        if (teamSize <= 0) {
            throw new IllegalArgumentException("팀 크기는 0보다 커야 합니다");
        }
        if (pickBanTime <= 0) {
            throw new IllegalArgumentException("픽밴 시간은 0보다 커야 합니다");
        }

        if (mode == TemplateCatalog.Mode.AUCTION) {
            if (budget == null) {
                throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
            }
            if (budget <= 0) {
                throw new IllegalArgumentException("예산은 0보다 커야 합니다");
            }
            if (minBidUnit == null) {
                throw new IllegalArgumentException("경매 템플릿에는 최소 입찰 단위가 필요합니다");
            }
            if (minBidUnit <= 0) {
                throw new IllegalArgumentException("최소 입찰 단위는 0보다 커야 합니다");
            }
            if (positionLimit != null && positionLimit <= 0) {
                throw new IllegalArgumentException("포지션 제한은 0보다 커야 합니다");
            }
            if (draftOrderStrategy != null) {
                throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
            }
        }

        if (mode == TemplateCatalog.Mode.DRAFT) {
            if (budget != null) {
                throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
            }
            if (minBidUnit != null) {
                throw new IllegalArgumentException("드래프트 템플릿에는 최소 입찰 단위를 지정할 수 없습니다");
            }
            if (positionLimit != null) {
                throw new IllegalArgumentException("드래프트 템플릿에는 포지션 제한을 지정할 수 없습니다");
            }
            if (draftOrderStrategy == null) {
                throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
            }
        }

        this.gameType = gameType;
        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.pickBanTime = pickBanTime;
        this.minBidUnit = minBidUnit;
        this.positionLimit = positionLimit;
        this.draftOrderStrategy = draftOrderStrategy;
    }

    public static TemplateConfiguration auction(
        TemplateCatalog.GameType gameType,
        int teamCount,
        int teamSize,
        int budget,
        int pickBanTime,
        int minBidUnit,
        Integer positionLimit
    ) {
        return new TemplateConfiguration(
            gameType,
            TemplateCatalog.Mode.AUCTION,
            teamCount,
            teamSize,
            budget,
            pickBanTime,
            minBidUnit,
            positionLimit,
            null
        );
    }

    public static TemplateConfiguration draft(
        TemplateCatalog.GameType gameType,
        int teamCount,
        int teamSize,
        int pickBanTime,
        TemplateCatalog.DraftOrderStrategy strategy
    ) {
        return new TemplateConfiguration(
            gameType,
            TemplateCatalog.Mode.DRAFT,
            teamCount,
            teamSize,
            null,
            pickBanTime,
            null,
            null,
            strategy
        );
    }

    public static TemplateConfiguration from(
        TemplateCatalog.GameType gameType,
        TemplateCatalog.Mode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        Integer minBidUnit,
        Integer positionLimit,
        TemplateCatalog.DraftOrderStrategy draftOrderStrategy
    ) {
        return switch (mode) {
            case AUCTION -> {
                if (draftOrderStrategy != null) {
                    throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
                }
                if (budget == null) {
                    throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
                }
                if (minBidUnit == null) {
                    throw new IllegalArgumentException("경매 템플릿에는 최소 입찰 단위가 필요합니다");
                }
                yield auction(gameType, teamCount, teamSize, budget, pickBanTime, minBidUnit, positionLimit);
            }
            case DRAFT -> {
                if (budget != null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
                }
                if (minBidUnit != null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 최소 입찰 단위를 지정할 수 없습니다");
                }
                if (positionLimit != null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 포지션 제한을 지정할 수 없습니다");
                }
                if (draftOrderStrategy == null) {
                    throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
                }
                yield draft(gameType, teamCount, teamSize, pickBanTime, draftOrderStrategy);
            }
        };
    }

    public int requiredPlayerCount() {
        return teamCount * (teamSize - 1);
    }
}
