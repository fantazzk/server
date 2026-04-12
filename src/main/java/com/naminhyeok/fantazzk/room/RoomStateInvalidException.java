package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.InvalidDomainStateException;

class RoomStateInvalidException extends InvalidDomainStateException {
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

    static RoomStateInvalidException draftTurnMissing() {
        return new RoomStateInvalidException("드래프트 턴 정보를 찾을 수 없습니다");
    }

    static RoomStateInvalidException leaderMissing(TeamLeaderId leaderId) {
        return new RoomStateInvalidException("팀장을 찾을 수 없습니다: " + leaderIdValue(leaderId));
    }

    private static String leaderIdValue(TeamLeaderId leaderId) {
        return leaderId == null ? "null" : leaderId.value();
    }
}
