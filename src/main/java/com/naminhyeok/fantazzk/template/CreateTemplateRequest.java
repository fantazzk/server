package com.naminhyeok.fantazzk.template;

import java.util.List;

public record CreateTemplateRequest(
    String name,
    TemplateMode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    DraftOrderStrategy draftOrderStrategy,
    List<String> playerNames
) {
    CreateTemplateCommand toCommand() {
        return switch (mode) {
            case AUCTION -> {
                if (draftOrderStrategy != null) {
                    throw TemplateException.invalidRequest("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
                }
                if (budget == null) {
                    throw TemplateException.invalidRequest("경매 템플릿에는 예산이 필요합니다");
                }
                yield new CreateTemplateCommand.Auction(name, teamCount, teamSize, budget, playerNames);
            }
            case DRAFT -> {
                if (budget != null) {
                    throw TemplateException.invalidRequest("드래프트 템플릿에는 예산을 지정할 수 없습니다");
                }
                if (draftOrderStrategy == null) {
                    throw TemplateException.invalidRequest("드래프트 템플릿에는 순서 전략이 필요합니다");
                }
                yield new CreateTemplateCommand.Draft(name, teamCount, teamSize, draftOrderStrategy, playerNames);
            }
        };
    }
}
