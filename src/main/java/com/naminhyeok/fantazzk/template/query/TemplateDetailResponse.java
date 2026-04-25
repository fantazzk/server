package com.naminhyeok.fantazzk.template.query;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "템플릿 상세 조회 응답")
public record TemplateDetailResponse(
    @Schema(description = "템플릿 ID", example = "11111111-1111-1111-1111-111111111111")
    String id,
    @Schema(description = "템플릿 이름", example = "LOL 2인 드래프트")
    String name,
    @Schema(description = "FE가 관리하는 게임 타입 메타데이터", example = "LEAGUE_OF_LEGENDS", nullable = true)
    String gameType,
    @Schema(description = "게임 모드", example = "DRAFT")
    TemplateCatalog.Mode mode,
    @Schema(description = "팀 수", example = "2")
    int teamCount,
    @Schema(description = "팀 전체 크기", example = "3")
    int teamSize,
    @Schema(description = "경매 예산", example = "300", nullable = true)
    Integer budget,
    @Schema(description = "턴 제한 시간(초)", example = "30")
    Integer pickBanTime,
    @Schema(description = "최소 입찰 증가 단위", example = "10", nullable = true)
    Integer minBidUnit,
    @Schema(description = "드래프트 순서 전략", example = "SNAKE", nullable = true)
    TemplateCatalog.DraftOrderStrategy draftOrderStrategy,
    @Schema(description = "선수 풀 목록")
    List<TemplatePlayerResponse> players
) {
    public static TemplateDetailResponse from(Template template) {
        return new TemplateDetailResponse(
            template.getId().templateId().toString(),
            template.getName(),
            template.getGameType(),
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getPickBanTime(),
            template.getMinBidUnit(),
            template.getDraftOrderStrategy(),
            template.getPlayers().stream().map(TemplatePlayerResponse::from).toList()
        );
    }
}
