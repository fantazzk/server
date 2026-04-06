package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "room_player")
public class RoomPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    private Long roomIdValue;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlayerStatus status;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomPlayer() {
        this((Long) null, (Room) null, null, "", PlayerStatus.AVAILABLE, 0, Instant.now(), Instant.now());
    }

    public RoomPlayer(
        Long roomPlayerId,
        Long roomId,
        String name,
        PlayerStatus status,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomPlayerId != null && roomPlayerId != 0L ? roomPlayerId : null,
            null,
            roomId != null && roomId != 0L ? roomId : null,
            name,
            status,
            displayOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomPlayer(
        RoomPlayerId roomPlayerId,
        RoomId roomId,
        String name,
        PlayerStatus status,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomPlayerId != null ? roomPlayerId.value() : null,
            roomId != null ? roomId.getValue() : null,
            name,
            status,
            displayOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomPlayer(Long roomId, String name, PlayerStatus status, int displayOrder, Instant createdAt, Instant updatedAt) {
        this(null, roomId, name, status, displayOrder, createdAt, updatedAt);
    }

    public RoomPlayer(Long roomId, String name, int displayOrder, Instant createdAt, Instant updatedAt) {
        this(roomId, name, PlayerStatus.AVAILABLE, displayOrder, createdAt, updatedAt);
    }

    public RoomPlayer(String name, int displayOrder) {
        this((Long) null, null, name, PlayerStatus.AVAILABLE, displayOrder, Instant.now(), Instant.now());
    }

    public RoomPlayer(Long roomId, String name, PlayerStatus status, int displayOrder) {
        this(roomId, name, status, displayOrder, Instant.now(), Instant.now());
    }

    public RoomPlayer(Long roomId, String name, int displayOrder) {
        this(roomId, name, PlayerStatus.AVAILABLE, displayOrder, Instant.now(), Instant.now());
    }

    private RoomPlayer(
        Long roomPlayerId,
        Room room,
        Long roomIdValue,
        String name,
        PlayerStatus status,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.persistentId = roomPlayerId;
        this.room = room;
        this.roomIdValue = roomIdValue;
        this.name = name;
        this.status = status;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RoomPlayerId getId() {
        return this.persistentId == null ? null : new RoomPlayerId(this.persistentId);
    }

    public long getRoomPlayerId() {
        return this.persistentId == null ? 0L : this.persistentId;
    }

    public long getRoomId() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue == null ? 0L : this.roomIdValue;
    }

    public String getName() {
        return name;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public RoomPlayer assign() {
        if (this.status != PlayerStatus.AVAILABLE) {
            throw new IllegalStateException("선수를 배정할 수 없습니다");
        }

        this.status = PlayerStatus.ASSIGNED;
        return this;
    }

    public RoomPlayer moveToBack(int displayOrder) {
        if (this.status != PlayerStatus.AVAILABLE) {
            throw new IllegalStateException("선수를 뒤로 보낼 수 없습니다");
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("순서는 0 이상이어야 합니다");
        }
        if (displayOrder <= this.displayOrder) {
            throw new IllegalArgumentException("현재 순서보다 뒤로만 이동할 수 있습니다");
        }
        this.displayOrder = displayOrder;
        return this;
    }

    public boolean isAvailable() {
        return this.status == PlayerStatus.AVAILABLE;
    }

    public RoomPlayer attach(Room room) {
        this.room = room;
        this.roomIdValue = room.getRoomId();
        return this;
    }

    public RoomPlayer detachCopy() {
        return new RoomPlayer(this.getRoomPlayerId(), roomIdOrNull(), this.name, this.status, this.displayOrder, this.createdAt, this.updatedAt);
    }

    public RoomPlayer copy(
        long roomPlayerId,
        long roomId,
        String name,
        PlayerStatus status,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new RoomPlayer(roomPlayerId, roomId, name, status, displayOrder, createdAt, updatedAt);
    }

    public RoomPlayer copy() {
        return detachCopy();
    }

    private Long roomIdOrNull() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue;
    }
}
