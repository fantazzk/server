package com.naminhyeok.fantazzk.template.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.domain.TeamBuildingMode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "템플릿 생성 요청입니다.")
public final class CreateTemplateRequest {
    @Schema(description = "템플릿 이름입니다.", example = "주말 풋살 경매전")
    private final String name;

    @Schema(description = "팀 빌딩 모드입니다.", example = "AUCTION")
    private final TeamBuildingMode mode;

    @Schema(description = "팀 수입니다.", example = "2")
    private final int teamCount;

    @Schema(description = "팀별 선수 수입니다.", example = "3")
    private final int teamSize;

    @Schema(description = "AUCTION 모드일 때 사용할 팀별 예산입니다. DRAFT 모드에서는 null 입니다.", example = "300", nullable = true)
    private final Integer budget;

    @Schema(description = "DRAFT 모드일 때 사용할 픽 순서 전략입니다. AUCTION 모드에서는 null 입니다.", example = "SNAKE", nullable = true)
    private final DraftOrderStrategy draftOrderStrategy;

    @ArraySchema(
        arraySchema = @Schema(description = "방 생성 시 복제될 선수 이름 목록입니다. 입력 순서가 displayOrder 가 됩니다."),
        schema = @Schema(example = "김민수")
    )
    @Schema(description = "선수 목록")
    private final List<String> playerNames;

    @JsonCreator
    public CreateTemplateRequest(
        @JsonProperty("name") String name,
        @JsonProperty("mode") TeamBuildingMode mode,
        @JsonProperty("teamCount") int teamCount,
        @JsonProperty("teamSize") int teamSize,
        @JsonProperty("budget") Integer budget,
        @JsonProperty("draftOrderStrategy") DraftOrderStrategy draftOrderStrategy,
        @JsonProperty("playerNames") List<String> playerNames
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.teamCount = teamCount;
        this.teamSize = teamSize;
        this.budget = budget;
        this.draftOrderStrategy = draftOrderStrategy;
        this.playerNames = List.copyOf(Objects.requireNonNull(playerNames, "playerNames must not be null"));
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

    public List<String> getPlayerNames() {
        return playerNames;
    }
}
