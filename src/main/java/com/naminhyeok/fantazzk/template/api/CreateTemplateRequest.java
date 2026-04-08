package com.naminhyeok.fantazzk.template.api;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand;
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.domain.TemplateErrorType;
import com.naminhyeok.fantazzk.template.domain.TemplateMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CreateTemplateRequest(
    @NotBlank(message = "템플릿 이름은 비어 있을 수 없습니다") String name,
    @NotNull(message = "템플릿 모드는 필수입니다") TemplateMode mode,
    @Positive(message = "팀 수는 1 이상이어야 합니다") int teamCount,
    @Positive(message = "팀 크기는 1 이상이어야 합니다") int teamSize,
    Integer budget,
    DraftOrderStrategy draftOrderStrategy,
    @NotEmpty(message = "선수 목록은 비어 있을 수 없습니다") List<String> playerNames
) {
    CreateTemplateCommand toCommand() {
        return switch (mode) {
            case AUCTION -> {
                if (draftOrderStrategy != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_DRAFT_ORDER_STRATEGY_NOT_ALLOWED);
                }
                if (budget == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_BUDGET_REQUIRED);
                }
                yield new CreateTemplateCommand.Auction(name, teamCount, teamSize, budget, playerNames);
            }
            case DRAFT -> {
                if (budget != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_BUDGET_NOT_ALLOWED);
                }
                if (draftOrderStrategy == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_ORDER_STRATEGY_REQUIRED);
                }
                yield new CreateTemplateCommand.Draft(name, teamCount, teamSize, draftOrderStrategy, playerNames);
            }
        };
    }
}
