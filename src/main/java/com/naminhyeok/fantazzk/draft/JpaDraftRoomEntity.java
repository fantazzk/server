package com.naminhyeok.fantazzk.draft;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "draft_room")
class JpaDraftRoomEntity {
    @Id
    @Column(name = "room_code", nullable = false, updatable = false)
    private String roomCode;

    @Column(name = "state_json", nullable = false, columnDefinition = "TEXT")
    private String stateJson;

    protected JpaDraftRoomEntity() {
    }

    JpaDraftRoomEntity(String roomCode, String stateJson) {
        this.roomCode = roomCode;
        this.stateJson = stateJson;
    }

    String roomCode() {
        return roomCode;
    }

    String stateJson() {
        return stateJson;
    }
}
