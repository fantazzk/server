package com.naminhyeok.fantazzk.template.web;

import com.naminhyeok.fantazzk.template.CreateTemplateInput;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

record CreateTemplateRequest(
    @NotBlank(message = "템플릿 이름은 비어 있을 수 없습니다") String name,
    @NotNull(message = "템플릿 모드는 필수입니다") TemplateCatalog.Mode mode,
    @Positive(message = "팀 수는 1 이상이어야 합니다") int teamCount,
    @Positive(message = "팀 크기는 1 이상이어야 합니다") int teamSize,
    Integer budget,
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    @NotEmpty(message = "선수 목록은 비어 있을 수 없습니다") List<String> playerNames
) {
    CreateTemplateInput toInput() {
        return new CreateTemplateInput(name, mode, teamCount, teamSize, budget, draftOrderStrategy, playerNames);
    }
}
