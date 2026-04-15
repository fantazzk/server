package com.naminhyeok.fantazzk.room;

import java.util.Objects;

record RoomDetails(
    Room room,
    Game game
) {
    RoomDetails {
        Objects.requireNonNull(room, "room must not be null");
    }

    static RoomDetails from(Room room) {
        return new RoomDetails(room, null);
    }
}
