package com.naminhyeok.fantazzk.room;

record RoomStartResponse(String gameId) {
    static RoomStartResponse from(Game game) {
        return new RoomStartResponse(game.getId().gameId().toString());
    }
}
