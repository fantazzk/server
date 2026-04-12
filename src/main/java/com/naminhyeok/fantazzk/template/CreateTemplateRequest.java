package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.CoreException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.stream.IntStream;

record CreateTemplateRequest(
    @NotBlank(message = "템플릿 이름은 비어 있을 수 없습니다") String name,
    @NotNull(message = "게임 타입은 필수입니다") GameType gameType,
    @NotNull(message = "템플릿 모드는 필수입니다") TemplateCatalog.Mode mode,
    @Positive(message = "팀 수는 1 이상이어야 합니다") int teamCount,
    @Positive(message = "팀 크기는 1 이상이어야 합니다") int teamSize,
    @Positive(message = "픽밴 시간은 1 이상이어야 합니다") int pickBanTime,
    Integer budget,
    Integer minBidUnit,
    Integer positionLimit,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    @NotEmpty(message = "선수 목록은 비어 있을 수 없습니다") List<@Valid PlayerRequest> players
) {
    record PlayerRequest(
        @NotBlank(message = "선수 이름은 비어 있을 수 없습니다") String name,
        @NotBlank(message = "선수 포지션은 비어 있을 수 없습니다") String position
    ) {
    }

    CreateTemplateCommand toCommand() {
        return switch (mode) {
            case AUCTION -> {
                if (draftOrderStrategy != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_DRAFT_ORDER_STRATEGY_NOT_ALLOWED);
                }
                if (budget == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_BUDGET_REQUIRED);
                }
                if (minBidUnit == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_MIN_BID_UNIT_REQUIRED);
                }
                yield new CreateTemplateCommand.Auction(
                    name,
                    gameType,
                    teamCount,
                    teamSize,
                    budget,
                    pickBanTime,
                    minBidUnit,
                    positionLimit,
                    toPlayers()
                );
            }
            case DRAFT -> {
                if (budget != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_BUDGET_NOT_ALLOWED);
                }
                if (minBidUnit != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_MIN_BID_UNIT_NOT_ALLOWED);
                }
                if (positionLimit != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_POSITION_LIMIT_NOT_ALLOWED);
                }
                if (draftOrderStrategy == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_ORDER_STRATEGY_REQUIRED);
                }
                yield new CreateTemplateCommand.Draft(
                    name,
                    gameType,
                    teamCount,
                    teamSize,
                    pickBanTime,
                    DraftOrderStrategy.valueOf(draftOrderStrategy.name()),
                    toPlayers()
                );
            }
        };
    }

    private List<CreateTemplateCommand.Player> toPlayers() {
        return IntStream.range(0, players.size())
            .mapToObj(index -> new CreateTemplateCommand.Player(players.get(index).name(), players.get(index).position(), index))
            .toList();
    }
}
