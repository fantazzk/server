package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomId;
import java.time.Instant;
import java.util.Objects;

public final class RoomBid {
    private final RoomBidId roomBidId;
    private final RoomId roomId;
    private final int round;
    private final String teamLeaderId;
    private final int amount;
    private final Instant createdAt;
    private final Instant updatedAt;

    private RoomBid(
            RoomBidId roomBidId,
            RoomId roomId,
            int round,
            String teamLeaderId,
            int amount,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.roomBidId = roomBidId == null ? RoomBidId.random() : roomBidId;
        this.roomId = Objects.requireNonNull(roomId, "roomId");
        this.round = round;
        this.teamLeaderId = requireText(teamLeaderId, "팀장 식별자는 비어 있을 수 없습니다");
        this.amount = amount;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static RoomBid create(RoomId roomId, int round, String teamLeaderId, int amount) {
        Instant now = Instant.now();
        return new RoomBid(null, roomId, round, teamLeaderId, amount, now, now);
    }

    public static RoomBid restore(
            RoomBidId roomBidId,
            RoomId roomId,
            int round,
            String teamLeaderId,
            int amount,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new RoomBid(roomBidId, roomId, round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public RoomBid copy() {
        return restore(roomBidId, roomId, round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public RoomBidId getRoomBidId() {
        return roomBidId;
    }

    public RoomId getRoomId() {
        return roomId;
    }

    public int getRound() {
        return round;
    }

    public String getTeamLeaderId() {
        return teamLeaderId;
    }

    public int getAmount() {
        return amount;
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
        if (!(other instanceof RoomBid roomBid)) {
            return false;
        }
        return round == roomBid.round
                && amount == roomBid.amount
                && roomBidId.equals(roomBid.roomBidId)
                && roomId.equals(roomBid.roomId)
                && teamLeaderId.equals(roomBid.teamLeaderId)
                && createdAt.equals(roomBid.createdAt)
                && updatedAt.equals(roomBid.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomBidId, roomId, round, teamLeaderId, amount, createdAt, updatedAt);
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
