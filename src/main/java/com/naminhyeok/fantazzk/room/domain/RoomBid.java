package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "room_bid")
public class RoomBid {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "round", nullable = false)
    private int round;

    @Column(name = "team_leader_id", nullable = false, length = 36)
    private String teamLeaderId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomBid() {
        this(null, Room.reference(RoomId.random()), 1, "leader", 1, Instant.now(), Instant.now());
    }

    private RoomBid(
            @Nullable RoomBidId roomBidId,
            Room room,
            int round,
            String teamLeaderId,
            int amount,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.persistentId = roomBidId == null ? UUID.randomUUID() : roomBidId.getValue();
        this.room = Objects.requireNonNull(room, "room");
        this.round = round;
        this.teamLeaderId = requireText(teamLeaderId, "팀장 식별자는 비어 있을 수 없습니다");
        this.amount = amount;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    private RoomBid(
            @Nullable RoomBidId roomBidId,
            RoomId roomId,
            int round,
            String teamLeaderId,
            int amount,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(roomBidId, Room.reference(roomId), round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public static RoomBid create(RoomId roomId, int round, String teamLeaderId, int amount) {
        Instant now = Instant.now();
        return new RoomBid(null, roomId, round, teamLeaderId, amount, now, now);
    }

    public static RoomBid restore(
            @Nullable RoomBidId roomBidId,
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
        return restore(getRoomBidId(), getRoomId(), round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public RoomBidId getRoomBidId() {
        return RoomBidId.from(Objects.requireNonNull(persistentId, "roomBidId is not assigned"));
    }

    public RoomId getRoomId() {
        return room.getRoomId();
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

    void attach(Room room) {
        this.room = Objects.requireNonNull(room, "room");
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
                && getRoomBidId().equals(roomBid.getRoomBidId())
                && getRoomId().equals(roomBid.getRoomId())
                && teamLeaderId.equals(roomBid.teamLeaderId)
                && createdAt.equals(roomBid.createdAt)
                && updatedAt.equals(roomBid.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoomBidId(), getRoomId(), round, teamLeaderId, amount, createdAt, updatedAt);
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
