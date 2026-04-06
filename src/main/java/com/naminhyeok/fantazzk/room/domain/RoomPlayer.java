package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.Nullable;

@Entity
@Table(name = "room_player")
public class RoomPlayer {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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
        this(null, Room.reference(RoomId.random()), "placeholder", PlayerStatus.AVAILABLE, 0, Instant.now(), Instant.now());
    }

    private RoomPlayer(
            @Nullable RoomPlayerId roomPlayerId,
            Room room,
            String name,
            PlayerStatus status,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.persistentId = roomPlayerId == null ? UUID.randomUUID() : roomPlayerId.getValue();
        this.room = Objects.requireNonNull(room, "room");
        this.name = requireText(name, "선수 이름은 비어 있을 수 없습니다");
        this.status = Objects.requireNonNull(status, "status");
        this.displayOrder = displayOrder;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    private RoomPlayer(
            @Nullable RoomPlayerId roomPlayerId,
            RoomId roomId,
            String name,
            PlayerStatus status,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(roomPlayerId, Room.reference(roomId), name, status, displayOrder, createdAt, updatedAt);
    }

    public static RoomPlayer create(RoomId roomId, String name, int displayOrder) {
        Instant now = Instant.now();
        return new RoomPlayer(null, roomId, name, PlayerStatus.AVAILABLE, displayOrder, now, now);
    }

    public static RoomPlayer restore(
            @Nullable RoomPlayerId roomPlayerId,
            RoomId roomId,
            String name,
            PlayerStatus status,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new RoomPlayer(roomPlayerId, roomId, name, status, displayOrder, createdAt, updatedAt);
    }

    public RoomPlayer assign() {
        checkState(status == PlayerStatus.AVAILABLE, "선수를 배정할 수 없습니다");
        status = PlayerStatus.ASSIGNED;
        return this;
    }

    public RoomPlayer moveToBack(int nextDisplayOrder) {
        checkState(status == PlayerStatus.AVAILABLE, "선수를 뒤로 보낼 수 없습니다");
        require(nextDisplayOrder >= 0, "순서는 0 이상이어야 합니다");
        require(nextDisplayOrder > displayOrder, "현재 순서보다 뒤로만 이동할 수 있습니다");
        displayOrder = nextDisplayOrder;
        return this;
    }

    public boolean isAvailable() {
        return status == PlayerStatus.AVAILABLE;
    }

    public RoomPlayer copy() {
        return restore(getRoomPlayerId(), getRoomId(), name, status, displayOrder, createdAt, updatedAt);
    }

    public RoomPlayerId getRoomPlayerId() {
        return RoomPlayerId.from(Objects.requireNonNull(persistentId, "roomPlayerId is not assigned"));
    }

    public RoomId getRoomId() {
        return room.getRoomId();
    }

    public String getName() {
        return name;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    void attach(Room room) {
        this.room = Objects.requireNonNull(room, "room");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomPlayer roomPlayer)) {
            return false;
        }
        return displayOrder == roomPlayer.displayOrder
                && getRoomPlayerId().equals(roomPlayer.getRoomPlayerId())
                && getRoomId().equals(roomPlayer.getRoomId())
                && name.equals(roomPlayer.name)
                && status == roomPlayer.status
                && createdAt.equals(roomPlayer.createdAt)
                && updatedAt.equals(roomPlayer.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoomPlayerId(), getRoomId(), name, status, displayOrder, createdAt, updatedAt);
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        require(!normalized.isEmpty(), message);
        return normalized;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
