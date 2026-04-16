package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.InvalidDomainStateException;

final class RoomStateInvalidException extends InvalidDomainStateException {
    private RoomStateInvalidException(String message) {
        super(message, RoomErrorType.ROOM_STATE_INVALID);
    }

    static RoomStateInvalidException auctionRoundMissing() {
        return new RoomStateInvalidException("경매 라운드를 찾을 수 없습니다");
    }

    static RoomStateInvalidException auctionTargetMissing() {
        return new RoomStateInvalidException("경매 대상 선수를 찾을 수 없습니다");
    }

    static RoomStateInvalidException auctionWinnerMissing(TeamLeaderId winnerId) {
        return new RoomStateInvalidException("경매 낙찰 팀장을 찾을 수 없습니다: " + leaderIdValue(winnerId));
    }

    static RoomStateInvalidException auctionWinnerBudgetMissing(TeamLeaderId winnerId) {
        return new RoomStateInvalidException("경매 낙찰 팀장의 남은 예산이 없습니다: " + leaderIdValue(winnerId));
    }

    static RoomStateInvalidException auctionWinnerBudgetExceeded(TeamLeaderId winnerId, Integer remainingBudget, int amount) {
        return new RoomStateInvalidException(
            "경매 정산 시 낙찰 팀장의 예산이 입찰가보다 작습니다: leaderId=%s, remainingBudget=%s, amount=%d".formatted(
                    leaderIdValue(winnerId),
                    remainingBudget,
                    amount
                )
        );
    }

    static RoomStateInvalidException draftTurnMissing() {
        return new RoomStateInvalidException("드래프트 턴 정보를 찾을 수 없습니다");
    }

    static RoomStateInvalidException draftPositionMissing(TeamLeaderId leaderId) {
        return new RoomStateInvalidException("드래프트 순서를 계산할 수 없습니다: draftPosition missing, leaderId=" + leaderIdValue(leaderId));
    }

    static RoomStateInvalidException draftLeaderOrderEmpty() {
        return new RoomStateInvalidException("드래프트 순서가 비어 있습니다");
    }

    static RoomStateInvalidException leaderMissing(TeamLeaderId leaderId) {
        return new RoomStateInvalidException("팀장을 찾을 수 없습니다: " + leaderIdValue(leaderId));
    }

    static RoomStateInvalidException playerMissing(RoomPlayerId playerId) {
        return new RoomStateInvalidException("선수를 찾을 수 없습니다: " + playerIdValue(playerId));
    }

    private static String leaderIdValue(TeamLeaderId leaderId) {
        return leaderId == null ? "null" : leaderId.value();
    }

    private static String playerIdValue(RoomPlayerId playerId) {
        return playerId == null ? "null" : Integer.toString(playerId.value());
    }
}
