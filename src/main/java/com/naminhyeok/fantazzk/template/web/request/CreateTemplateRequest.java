package com.naminhyeok.fantazzk.template.web.request;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand;
import com.naminhyeok.fantazzk.template.domain.TemplateErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.stream.IntStream;

@Schema(description = "템플릿 생성 요청")
public record CreateTemplateRequest(
    @Schema(description = "템플릿 이름", example = "LOL 2인 드래프트")
    @NotBlank(message = "템플릿 이름은 비어 있을 수 없습니다") String name,
    @Schema(description = "게임 타입", example = "LEAGUE_OF_LEGENDS")
    @NotNull(message = "게임 타입은 필수입니다") TemplateCatalog.GameType gameType,
    @Schema(description = "템플릿 모드", example = "DRAFT")
    @NotNull(message = "템플릿 모드는 필수입니다") TemplateCatalog.Mode mode,
    @Schema(description = "팀 수", example = "2")
    @Positive(message = "팀 수는 1 이상이어야 합니다") int teamCount,
    @Schema(description = "팀 전체 크기. 실제로 뽑아야 하는 선수 수는 `teamSize - 1` 입니다.", example = "3")
    @Positive(message = "팀 크기는 1 이상이어야 합니다") int teamSize,
    @Schema(description = "턴 제한 시간(초)", example = "30")
    @Positive(message = "픽밴 시간은 1 이상이어야 합니다") int pickBanTime,
    @Schema(description = "경매 모드에서만 사용되는 팀별 예산", example = "300", nullable = true)
    Integer budget,
    @Schema(description = "경매 모드에서만 사용되는 최소 입찰 증가 단위", example = "10", nullable = true)
    Integer minBidUnit,
    @Schema(description = "경매 모드에서만 사용되는 동일 포지션 최대 보유 인원", example = "1", nullable = true)
    Integer positionLimit,
    @Schema(description = "드래프트 모드에서만 사용되는 라운드 순서 전략", example = "SNAKE", nullable = true)
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    @Schema(description = "선수 풀 목록. 길이는 정확히 `teamCount * (teamSize - 1)` 이어야 합니다.")
    @NotEmpty(message = "선수 목록은 비어 있을 수 없습니다") List<@Valid PlayerRequest> players
) {
    @Schema(description = "템플릿 선수 입력")
    record PlayerRequest(
        @Schema(description = "선수 이름", example = "선수1")
        @NotBlank(message = "선수 이름은 비어 있을 수 없습니다") String name,
        @Schema(description = "게임 타입에 맞는 포지션 코드", example = "TOP")
        @NotBlank(message = "선수 포지션은 비어 있을 수 없습니다") String position
    ) {
    }

    public CreateTemplateCommand toCommand() {
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
                    draftOrderStrategy,
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
