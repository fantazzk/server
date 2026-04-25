package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Access(AccessType.FIELD)
@Embeddable
public class RoomPlayer {
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

    public RoomPlayer(RoomPlayerId id, String name, String position, int displayOrder) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.displayOrder = displayOrder;
        this.status = PlayerStatus.AVAILABLE;
    }
}
