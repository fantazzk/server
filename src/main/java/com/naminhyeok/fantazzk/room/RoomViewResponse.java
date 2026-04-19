package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "로비 화면의 source of truth")
record RoomViewResponse(
    @Schema(description = "방 코드", example = "ROOM01")
    String code,
    @Schema(description = "방 상태", example = "WAITING", allowableValues = {"WAITING", "STARTED"})
    String status,
    @Schema(description = "게임 모드", example = "DRAFT", allowableValues = {"DRAFT", "AUCTION"})
    String mode,
    @Schema(description = "총 팀 수", example = "2")
    int teamCount,
    @Schema(description = "팀 전체 크기", example = "3")
    int teamSize,
    @Schema(description = "경매 예산", example = "300", nullable = true)
    Integer budget,
    @Schema(description = "최소 입찰 증가 단위", example = "10", nullable = true)
    Integer minBidUnit,
    @Schema(description = "드래프트 순서 전략", example = "SNAKE", allowableValues = {"SNAKE", "FIXED"}, nullable = true)
    String draftOrderStrategy,
    @Schema(
        description = "로비 기준 시작 가능 상태. FE는 이 값을 그대로 시작 버튼 활성화/안내 문구에 활용하면 됩니다.",
        example = "STARTABLE",
        allowableValues = {"WAITING_FOR_LEADERS", "WAITING_FOR_DRAFT_POSITIONS", "STARTABLE", "NOT_WAITING"}
    )
    String startReadiness,
    @Schema(
        description = "방이 시작되면 채워지는 게임 ID. 값이 생기면 FE는 `/games/{gameId}` 기반 진행 화면으로 전환해야 합니다.",
        example = "00000000-0000-0000-0000-000000000201",
        nullable = true
    )
    String startedGameId,
    @Schema(description = "드래프트 로비에서만 제공되는 자리 현황", nullable = true)
    DraftOrderPreviewResponse draftOrderPreview,
    @Schema(description = "참가한 팀장 목록")
    List<TeamLeaderResponse> teamLeaders,
    @Schema(description = "로비에서 보여줄 선수 풀")
    List<RoomPlayerResponse> players
) {
    static RoomViewResponse from(Room room) {
        return new RoomViewResponse(
            room.getCode(),
            room.getStatus().name(),
            room.getMode().name(),
            room.getTeamCount(),
            room.getTeamSize(),
            room.getBudget(),
            room.getMinBidUnit(),
            room.getDraftOrderStrategy() == null ? null : room.getDraftOrderStrategy().name(),
            room.getStartReadiness().name(),
            room.getStartedGameId() == null ? null : room.getStartedGameId().gameId().toString(),
            DraftOrderPreviewResponse.from(room),
            room.getLeaders().stream().map(TeamLeaderResponse::from).toList(),
            room.getPlayers().stream().map(RoomPlayerResponse::from).toList()
        );
    }
}
