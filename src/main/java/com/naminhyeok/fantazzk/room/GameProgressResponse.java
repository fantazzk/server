package com.naminhyeok.fantazzk.room;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "현재 진행 정보. 모드에 따라 일부 필드만 사용됩니다.")
record GameProgressResponse(
    @Schema(description = "드래프트에서 현재 턴 인덱스", example = "1", nullable = true)
    Integer currentTurnIndex,
    @Schema(description = "현재 라운드 번호", example = "2", nullable = true)
    Integer currentRound,
    @Schema(description = "드래프트에서 현재 행동 가능한 팀장 ID", example = "leader-guest", nullable = true)
    String currentLeaderId,
    @Schema(description = "드래프트 현재 라운드의 전체 순서", nullable = true)
    List<String> currentRoundLeaderIds,
    @Schema(description = "경매 현재 라운드 종료 시각", example = "2026-04-19T12:00:45Z", nullable = true)
    Instant currentAuctionRoundEndsAt,
    @Schema(description = "현재 경매 대상 선수", nullable = true)
    AuctionTargetResponse currentAuctionTarget,
    @Schema(description = "현재 최고 입찰 금액", example = "150", nullable = true)
    Integer highestBidAmount,
    @Schema(description = "현재 최고가를 보유한 팀장 ID", example = "leader-guest", nullable = true)
    String leadingLeaderId,
    @Schema(description = "현재 라운드 누적 입찰 수", example = "2", nullable = true)
    Integer bidCount
) {
    static GameProgressResponse from(Game game) {
        if (game instanceof AuctionGame auctionGame) {
            if (auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
                return empty();
            }
            AuctionBid winningBid = auctionGame.currentWinningBid();
            return new GameProgressResponse(
                null,
                auctionGame.getCurrentRound() <= 0 ? null : auctionGame.getCurrentRound(),
                null,
                null,
                auctionGame.getCurrentRoundEndsAt(),
                AuctionTargetResponse.from(auctionGame.currentAuctionTarget()),
                winningBid == null ? null : winningBid.amount(),
                winningBid == null ? null : winningBid.teamLeaderId().value(),
                auctionGame.currentBidCount()
            );
        }
        if (game instanceof DraftGame draftGame) {
            if (draftGame.getStatus() != GameStatus.IN_PROGRESS) {
                return empty();
            }
            DraftProgress progress = draftGame.currentDraftProgress();
            return new GameProgressResponse(
                draftGame.getCurrentTurnIndex(),
                progress.currentRound(),
                progress.currentLeaderId(),
                progress.currentRoundLeaderIds(),
                null,
                null,
                null,
                null,
                null
            );
        }
        return empty();
    }

    private static GameProgressResponse empty() {
        return new GameProgressResponse(null, null, null, null, null, null, null, null, null);
    }
}
