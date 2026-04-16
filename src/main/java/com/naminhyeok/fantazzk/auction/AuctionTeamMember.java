package com.naminhyeok.fantazzk.auction;

final class AuctionTeamMember {
    private final String leaderId;
    private final int playerId;
    private final int sequence;

    AuctionTeamMember(String leaderId, int playerId, int sequence) {
        this.leaderId = leaderId;
        this.playerId = playerId;
        this.sequence = sequence;
    }

    String leaderId() {
        return leaderId;
    }

    int playerId() {
        return playerId;
    }

    int sequence() {
        return sequence;
    }
}
