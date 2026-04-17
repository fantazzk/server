package com.naminhyeok.fantazzk.auction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auction_room")
class JpaAuctionRoomEntity {
    @Id
    @Column(name = "room_code", nullable = false, updatable = false)
    private String roomCode;

    @Column(name = "snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;

    protected JpaAuctionRoomEntity() {
    }

    JpaAuctionRoomEntity(String roomCode, String snapshotJson) {
        this.roomCode = roomCode;
        this.snapshotJson = snapshotJson;
    }

    String roomCode() {
        return roomCode;
    }

    String snapshotJson() {
        return snapshotJson;
    }
}
