package com.naminhyeok.fantazzk.template.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "템플릿에 포함된 선수 정보입니다.")
public final class TemplatePlayerResponse {
    @Schema(description = "선수 이름입니다.", example = "김민수")
    private final String name;

    @Schema(description = "템플릿에 저장된 선수 순서입니다.", example = "0")
    private final int displayOrder;

    public TemplatePlayerResponse(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
