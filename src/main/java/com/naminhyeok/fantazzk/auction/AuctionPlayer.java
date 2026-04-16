package com.naminhyeok.fantazzk.auction;

final class AuctionPlayer {
    private final int playerId;
    private final String name;
    private final String position;
    private int displayOrder;
    private AuctionPlayerStatus status;

    AuctionPlayer(int playerId, String name, String position, int displayOrder) {
        this.playerId = playerId;
        this.name = name;
        this.position = position;
        this.displayOrder = displayOrder;
        this.status = AuctionPlayerStatus.AVAILABLE;
    }

    int playerId() {
        return playerId;
    }

    String name() {
        return name;
    }

    String position() {
        return position;
    }

    int displayOrder() {
        return displayOrder;
    }

    boolean available() {
        return status == AuctionPlayerStatus.AVAILABLE;
    }

    void assign() {
        status = AuctionPlayerStatus.ASSIGNED;
    }

    void moveToBack(int nextDisplayOrder) {
        displayOrder = nextDisplayOrder;
    }
}
