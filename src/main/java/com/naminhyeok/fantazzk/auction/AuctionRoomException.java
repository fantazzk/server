package com.naminhyeok.fantazzk.auction;

final class AuctionRoomException extends RuntimeException {
    private AuctionRoomException(String message) {
        super(message);
    }

    static AuctionRoomException roomNotFound(String code) {
        return new AuctionRoomException("경매 방을 찾을 수 없습니다: " + code);
    }

    static AuctionRoomException hostForbidden(String leaderId) {
        return new AuctionRoomException("호스트만 경매를 시작할 수 있습니다: " + leaderId);
    }

    static AuctionRoomException roomNotWaiting() {
        return new AuctionRoomException("대기 중인 경매 방이 아닙니다");
    }

    static AuctionRoomException roomNotInProgress() {
        return new AuctionRoomException("진행 중인 경매가 아닙니다");
    }

    static AuctionRoomException roundMissing() {
        return new AuctionRoomException("현재 경매 라운드가 없습니다");
    }

    static AuctionRoomException roundNotEnded() {
        return new AuctionRoomException("경매 라운드가 아직 종료되지 않았습니다");
    }

    static AuctionRoomException leaderLimitReached() {
        return new AuctionRoomException("경매 팀장 수가 가득 찼습니다");
    }

    static AuctionRoomException leadersNotFull() {
        return new AuctionRoomException("경매 팀장 수가 아직 채워지지 않았습니다");
    }

    static AuctionRoomException nicknameTaken(String nickname) {
        return new AuctionRoomException("이미 사용 중인 닉네임입니다: " + nickname);
    }

    static AuctionRoomException leaderMissing(String leaderId) {
        return new AuctionRoomException("방에 없는 팀장입니다: " + leaderId);
    }

    static AuctionRoomException bidderMissing(String leaderId) {
        return new AuctionRoomException("입찰자를 찾을 수 없습니다: " + leaderId);
    }

    static AuctionRoomException targetMissing() {
        return new AuctionRoomException("입찰할 선수가 없습니다");
    }

    static AuctionRoomException amountNotPositive() {
        return new AuctionRoomException("입찰 금액은 0보다 커야 합니다");
    }

    static AuctionRoomException budgetExceeded() {
        return new AuctionRoomException("예산이 부족합니다");
    }

    static AuctionRoomException tooLow() {
        return new AuctionRoomException("현재 최고가보다 낮거나 같은 입찰입니다");
    }

    static AuctionRoomException minUnitNotMet() {
        return new AuctionRoomException("최소 입찰 단위를 만족하지 않습니다");
    }

    static AuctionRoomException positionLimitExceeded() {
        return new AuctionRoomException("포지션 제한을 초과했습니다");
    }

    static AuctionRoomException winnerMissing(String leaderId) {
        return new AuctionRoomException("낙찰된 팀장을 찾을 수 없습니다: " + leaderId);
    }

    static AuctionRoomException playerMissing(int playerId) {
        return new AuctionRoomException("선수를 찾을 수 없습니다: " + playerId);
    }
}
