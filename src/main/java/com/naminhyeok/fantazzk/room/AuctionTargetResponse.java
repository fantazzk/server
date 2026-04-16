package com.naminhyeok.fantazzk.room;

record AuctionTargetResponse(
    int playerId,
    String name,
    String position
) {
    static AuctionTargetResponse from(RoomPlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetResponse(player.getId().value(), player.getName(), player.getPosition());
    }
}
