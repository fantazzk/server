package com.naminhyeok.fantazzk.room;

record AuctionTargetResponse(
    String name,
    String position
) {
    static AuctionTargetResponse from(RoomPlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.getName(), player.getPosition());
    }
}
