package com.naminhyeok.fantazzk.room;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
@Access(AccessType.FIELD)
@Embeddable
class RoomPlayer {
    @Column(name = "room_player_id")
    @Convert(converter = RoomPlayerId.JpaConverter.class)
    private RoomPlayerId id;
    @Column(name = "name")
    private String name;
    @Column(name = "position")
    private String position;
    @Column(name = "display_order")
    private int displayOrder;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PlayerStatus status;

    RoomPlayer() {
    }

    RoomPlayer(RoomPlayerId id, String name, String position, int displayOrder) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.displayOrder = displayOrder;
        this.status = PlayerStatus.AVAILABLE;
    }

    RoomPlayer(RoomPlayerId id, String name, String position, int displayOrder, PlayerStatus status) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.displayOrder = displayOrder;
        this.status = status;
    }

    public RoomPlayerId getId() {
        return id;
    }

    void assign() {
        this.status = PlayerStatus.ASSIGNED;
    }

    void moveToBack(int nextOrder) {
        this.displayOrder = nextOrder;
    }

    void sync(int displayOrder, PlayerStatus status) {
        this.displayOrder = displayOrder;
        this.status = status;
    }
}
