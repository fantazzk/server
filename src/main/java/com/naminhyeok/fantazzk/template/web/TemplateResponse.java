package com.naminhyeok.fantazzk.template.web;

import com.naminhyeok.fantazzk.template.application.TemplateDetail;
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.domain.TeamBuildingMode;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Schema(description = "템플릿 조회 API 의 성공 응답 payload 입니다.")
public final class TemplateResponse {
    @Schema(description = "템플릿 ID 입니다.", example = "00000000-0000-0000-0000-000000000001")
    private final String id;

    @Schema(description = "템플릿 이름입니다.", example = "주말 풋살 경매전")
    private final String name;

    @Schema(description = "팀 빌딩 모드입니다.", example = "AUCTION")
    private final TeamBuildingMode mode;

    @Schema(description = "팀 수입니다.", example = "2")
    private final int teamCount;

    @Schema(description = "팀별 선수 수입니다.", example = "3")
    private final int teamSize;

    @Schema(description = "AUCTION 모드일 때 사용하는 팀별 예산입니다.", example = "300", nullable = true)
    private final Integer budget;

    @Schema(description = "DRAFT 모드일 때 사용하는 픽 순서 전략입니다.", example = "SNAKE", nullable = true)
    private final DraftOrderStrategy draftOrderStrategy;

    @ArraySchema(
        arraySchema = @Schema(description = "상세 조회에서는 선수 목록이 포함됩니다. 목록 조회에서는 null 일 수 있습니다.", nullable = true),
        schema = @Schema(implementation = TemplatePlayerResponse.class)
    )
    @Schema(description = "선수 목록")
    private final List<TemplatePlayerResponse> players;

    public TemplateResponse(
        String id,
        String name,
        TeamBuildingMode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        DraftOrderStrategy draftOrderStrategy,
        List<TemplatePlayerResponse> players
    ) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        this.players = players;
    }

    public static TemplateResponse from(Template template, List<TemplatePlayer> players) {
        return new TemplateResponse(
            template.getId().getValue().toString(),
            template.getName(),
            template.getMode(),
            template.getTeamCount(),
            template.getTeamSize(),
            template.getBudget(),
            template.getDraftOrderStrategy(),
            players == null
                ? null
                : players
                    .stream()
                    .map(player -> new TemplatePlayerResponse(player.getName(), player.getDisplayOrder()))
                    .collect(Collectors.toList())
        );
    }

    public static TemplateResponse from(TemplateDetail detail) {
        return from(detail.getTemplate(), detail.getPlayers());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TeamBuildingMode getMode() {
        return mode;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public Integer getBudget() {
        return budget;
    }

    public DraftOrderStrategy getDraftOrderStrategy() {
        return draftOrderStrategy;
    }

    public List<TemplatePlayerResponse> getPlayers() {
        return players;
    }
}
