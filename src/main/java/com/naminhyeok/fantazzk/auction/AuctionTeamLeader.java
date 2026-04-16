package com.naminhyeok.fantazzk.auction;

final class AuctionTeamLeader {
    private final String leaderId;
    private final String nickname;
    private Integer remainingBudget;

    AuctionTeamLeader(String leaderId, String nickname, Integer remainingBudget) {
        this.leaderId = leaderId;
        this.nickname = nickname;
        this.remainingBudget = remainingBudget;
    }

    String leaderId() {
        return leaderId;
    }

    String nickname() {
        return nickname;
    }

    Integer remainingBudget() {
        return remainingBudget;
    }

    void spend(int amount) {
        if (remainingBudget == null) {
            return;
        }
        int nextBudget = remainingBudget - amount;
        if (nextBudget < 0) {
            throw AuctionRoomException.budgetExceeded();
        }
        remainingBudget = nextBudget;
    }
}
