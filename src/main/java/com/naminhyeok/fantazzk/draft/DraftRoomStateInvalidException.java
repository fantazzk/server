package com.naminhyeok.fantazzk.draft;

import com.naminhyeok.fantazzk.InvalidDomainStateException;

final class DraftRoomStateInvalidException extends InvalidDomainStateException {
    private DraftRoomStateInvalidException(String message) {
        super(message, null);
    }

    static DraftRoomStateInvalidException roomAlreadyStarted() {
        return new DraftRoomStateInvalidException("드래프트가 이미 시작되었습니다");
    }

    static DraftRoomStateInvalidException roomAlreadyCompleted() {
        return new DraftRoomStateInvalidException("드래프트가 이미 완료되었습니다");
    }

    static DraftRoomStateInvalidException roomNotWaiting() {
        return new DraftRoomStateInvalidException("대기 중인 드래프트 방이 아닙니다");
    }

    static DraftRoomStateInvalidException roomNotInProgress() {
        return new DraftRoomStateInvalidException("진행 중인 드래프트 방이 아닙니다");
    }

    static DraftRoomStateInvalidException roomNotReadyForStart(DraftRoomReadiness readiness) {
        return switch (readiness) {
            case WAITING_FOR_LEADERS -> new DraftRoomStateInvalidException("드래프트를 시작할 수 없습니다: 팀장이 부족합니다");
            case WAITING_FOR_DRAFT_POSITIONS -> new DraftRoomStateInvalidException("드래프트를 시작할 수 없습니다: 드래프트 자리가 아직 확정되지 않았습니다");
            case READY -> new DraftRoomStateInvalidException("드래프트를 시작할 수 있습니다");
            case IN_PROGRESS -> roomAlreadyStarted();
            case COMPLETED -> roomAlreadyCompleted();
        };
    }

    static DraftRoomStateInvalidException leaderMissing(String leaderId) {
        return new DraftRoomStateInvalidException("팀장을 찾을 수 없습니다: " + leaderId);
    }

    static DraftRoomStateInvalidException playerMissing(int playerId) {
        return new DraftRoomStateInvalidException("선수를 찾을 수 없습니다: " + playerId);
    }

    static DraftRoomStateInvalidException draftPositionMissing(String leaderId) {
        return new DraftRoomStateInvalidException("드래프트 순서를 계산할 수 없습니다: draftPosition missing, leaderId=" + leaderId);
    }

    static DraftRoomStateInvalidException draftLeaderOrderEmpty() {
        return new DraftRoomStateInvalidException("드래프트 순서가 비어 있습니다");
    }

    static DraftRoomStateInvalidException currentTurnMissing() {
        return new DraftRoomStateInvalidException("드래프트 턴 정보를 찾을 수 없습니다");
    }
}
