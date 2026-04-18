package com.naminhyeok.fantazzk.room;

public record AuctionTargetView(
    String name,
    String position
) {
    static AuctionTargetView from(RoomPlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetView(player.getName(), player.getPosition());
    }

    static AuctionTargetView from(GamePlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetView(player.name(), player.position());
    }
}
