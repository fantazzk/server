package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.domain.Game;

public record RoomStartResponse(String gameId) {
    public static RoomStartResponse from(Game game) {
        return new RoomStartResponse(game.getId().gameId().toString());
    }
}
