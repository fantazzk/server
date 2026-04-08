package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.CoreException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class TemplateManagementImpl implements TemplateManagement {
    private final CreateTemplate createTemplate;
    private final FindTemplates findTemplates;

    @Override
    public TemplateSummaryView create(CreateTemplateInput input) {
        return TemplateSummaryView.from(createTemplate.create(toCommand(input)));
    }

    @Override
    public TemplateDetailView getDetail(UUID templateId) {
        return TemplateDetailView.from(findTemplates.getDetail(new TemplateId(templateId)));
    }

    @Override
    public List<TemplateSummaryView> list() {
        return findTemplates.list().stream().map(TemplateSummaryView::from).toList();
    }

    private CreateTemplateCommand toCommand(CreateTemplateInput input) {
        return switch (input.mode()) {
            case AUCTION -> {
                if (input.draftOrderStrategy() != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_DRAFT_ORDER_STRATEGY_NOT_ALLOWED);
                }
                if (input.budget() == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_AUCTION_BUDGET_REQUIRED);
                }
                yield new CreateTemplateCommand.Auction(
                    input.name(),
                    input.teamCount(),
                    input.teamSize(),
                    input.budget(),
                    input.playerNames()
                );
            }
            case DRAFT -> {
                if (input.budget() != null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_BUDGET_NOT_ALLOWED);
                }
                if (input.draftOrderStrategy() == null) {
                    throw CoreException.of(TemplateErrorType.TEMPLATE_DRAFT_ORDER_STRATEGY_REQUIRED);
                }
                yield new CreateTemplateCommand.Draft(
                    input.name(),
                    input.teamCount(),
                    input.teamSize(),
                    DraftOrderStrategy.valueOf(input.draftOrderStrategy().name()),
                    input.playerNames()
                );
            }
        };
    }
}
