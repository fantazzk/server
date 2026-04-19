package com.naminhyeok.fantazzk.template.query;

import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "템플릿 선수 정보")
public record TemplatePlayerResponse(
    @Schema(description = "선수 이름", example = "선수1")
    String name,
    @Schema(description = "포지션 코드", example = "TOP")
    String position,
    @Schema(description = "화면 노출 순서", example = "0")
    int displayOrder
) {
    static TemplatePlayerResponse from(TemplatePlayer player) {
        return new TemplatePlayerResponse(player.name(), player.position(), player.displayOrder());
    }
}
