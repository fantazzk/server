package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomId;
import java.time.Instant;
import java.util.Objects;

public final class RoomPlayer {
    private final RoomPlayerId roomPlayerId;
    private final RoomId roomId;
    private final String name;
    private PlayerStatus status;
    private int displayOrder;
    private final Instant createdAt;
    private final Instant updatedAt;

    private RoomPlayer(
            RoomPlayerId roomPlayerId,
            RoomId roomId,
            String name,
            PlayerStatus status,
            int displayOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.roomPlayerId = roomPlayerId == null ? RoomPlayerId.random() : roomPlayerId;
        this.roomId = Objects.requireNonNull(roomId, "roomId");
        this.name = requireText(name, "선수 이름은 비어 있을 수 없습니다");
        this.status = Objects.requireNonNull(status, "status");
        this.displayOrder = displayOrder;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static RoomPlayer create(RoomId roomId, String name, int displayOrder) {
        Instant now = Instant.now();
        return new RoomPlayer(null, roomId, name, PlayerStatus.AVAILABLE, displayOrder, now, now);
    }

    public static RoomPlayer restore(
            RoomPlayerId roomPlayerId,
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
        return restore(roomPlayerId, roomId, name, status, displayOrder, createdAt, updatedAt);
    }

    public RoomPlayerId getRoomPlayerId() {
        return roomPlayerId;
    }

    public RoomId getRoomId() {
        return roomId;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomPlayer roomPlayer)) {
            return false;
        }
        return displayOrder == roomPlayer.displayOrder
                && roomPlayerId.equals(roomPlayer.roomPlayerId)
                && roomId.equals(roomPlayer.roomId)
                && name.equals(roomPlayer.name)
                && status == roomPlayer.status
                && createdAt.equals(roomPlayer.createdAt)
                && updatedAt.equals(roomPlayer.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomPlayerId, roomId, name, status, displayOrder, createdAt, updatedAt);
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
