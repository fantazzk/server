package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RosterMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "진행 화면의 source of truth")
public record GameDetailResponse(
    @Schema(description = "게임 ID", example = "00000000-0000-0000-0000-000000000201")
    String gameId,
    @Schema(description = "원본 방 코드", example = "ROOM01")
    String roomCode,
    @Schema(description = "게임 모드", example = "AUCTION", allowableValues = {"DRAFT", "AUCTION"})
    String mode,
    @Schema(description = "게임 상태", example = "IN_PROGRESS", allowableValues = {"IN_PROGRESS", "COMPLETED"})
    String status,
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
    @Schema(description = "참가 팀장 목록")
    List<GameParticipantResponse> participants,
    @Schema(description = "선수 풀. 드래프트에서는 원래 displayOrder, 경매에서는 현재 경매 순서 기준으로 정렬됩니다.")
    List<GamePlayerResponse> playerPool,
    @Schema(description = "지금까지 확정된 멤버 목록")
    List<GameMemberResponse> roster,
    @Schema(description = "드래프트 게임 현재 진행 정보", nullable = true)
    DraftProgressResponse draftProgress,
    @Schema(description = "경매 게임 현재 진행 정보", nullable = true)
    AuctionProgressResponse auctionProgress
) {
    public static GameDetailResponse from(Game game) {
        return new GameDetailResponse(
            game.getId().gameId().toString(),
            game.getRoomCode(),
            modeOf(game),
            game.getStatus().name(),
            game.getTeamCount(),
            game.getTeamSize(),
            game.getBudget(),
            game.getMinBidUnit(),
            game.getDraftOrderStrategy() == null ? null : game.getDraftOrderStrategy().name(),
            game.getParticipants().stream().map(GameParticipantResponse::from).toList(),
            playerPoolOf(game),
            rosterOf(game),
            DraftProgressResponse.from(game),
            AuctionProgressResponse.from(game)
        );
    }

    public static List<GameMemberResponse> rosterOf(Game game) {
        return membersOf(game).stream().map(GameMemberResponse::from).toList();
    }

    private static String modeOf(Game game) {
        return game instanceof AuctionGame ? RoomMode.AUCTION.name() : RoomMode.DRAFT.name();
    }

    private static List<GamePlayerResponse> playerPoolOf(Game game) {
        Set<String> assignedPlayerNames = membersOf(game).stream().map(RosterMember::playerName).collect(Collectors.toSet());
        if (game instanceof AuctionGame) {
            List<GamePlayer> playerPool = game.getPlayerPool();
            return playerPool.stream()
                .map(player -> GamePlayerResponse.from(
                    player,
                    playerPool.indexOf(player),
                    assignedPlayerNames.contains(player.name())
                ))
                .toList();
        }
        return game.getPlayerPool().stream()
            .sorted(Comparator.comparingInt(GamePlayer::displayOrder))
            .map(player -> GamePlayerResponse.from(
                player,
                player.displayOrder(),
                assignedPlayerNames.contains(player.name())
            ))
            .toList();
    }

    private static List<RosterMember> membersOf(Game game) {
        if (game instanceof AuctionGame auctionGame) {
            return auctionGame.getMembers();
        }
        if (game instanceof DraftGame draftGame) {
            return draftGame.getMembers();
        }
        return List.of();
    }
}
