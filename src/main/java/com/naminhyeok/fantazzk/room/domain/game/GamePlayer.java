package com.naminhyeok.fantazzk.room.domain.game;

import com.naminhyeok.fantazzk.room.domain.event.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import org.jmolecules.ddd.types.ValueObject;

@Access(AccessType.FIELD)
@Embeddable
@EqualsAndHashCode
public final class GamePlayer implements ValueObject {
    @Column(name = "player_id")
    @Convert(converter = RoomPlayerId.JpaConverter.class)
    private RoomPlayerId playerId;
    @Column(name = "name")
    private String name;
    @Column(name = "position")
    private String position;
    @Column(name = "display_order")
    private int displayOrder;

    GamePlayer() {
    }

    public GamePlayer(RoomPlayerId playerId, String name, String position, int displayOrder) {
        this.playerId = java.util.Objects.requireNonNull(playerId, "playerId must not be null");
        this.name = java.util.Objects.requireNonNull(name, "name must not be null");
        this.position = java.util.Objects.requireNonNull(position, "position must not be null");
        this.displayOrder = displayOrder;
    }

    public RoomPlayerId playerId() {
        return playerId;
    }

    public String name() {
        return name;
    }

    public String position() {
        return position;
    }

    public int displayOrder() {
        return displayOrder;
    }
}
