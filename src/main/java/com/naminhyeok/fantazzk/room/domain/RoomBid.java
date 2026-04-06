package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "room_bid")
public class RoomBid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    private Long roomIdValue;

    @Column(name = "round", nullable = false)
    private int round;

    @Column(name = "team_leader_id", nullable = false)
    private String teamLeaderId;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomBid() {
        this((Long) null, (Room) null, null, 0, "", 0, Instant.now(), Instant.now());
    }

    public RoomBid(
        Long roomBidId,
        Long roomId,
        int round,
        String teamLeaderId,
        int amount,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomBidId != null && roomBidId != 0L ? roomBidId : null,
            null,
            roomId != null && roomId != 0L ? roomId : null,
            round,
            teamLeaderId,
            amount,
            createdAt,
            updatedAt
        );
    }

    public RoomBid(
        RoomBidId roomBidId,
        RoomId roomId,
        int round,
        String teamLeaderId,
        int amount,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomBidId != null ? roomBidId.value() : null,
            roomId != null ? roomId.getValue() : null,
            round,
            teamLeaderId,
            amount,
            createdAt,
            updatedAt
        );
    }

    public RoomBid(
        Long roomId,
        int round,
        String teamLeaderId,
        int amount,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(null, roomId, round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public RoomBid(int round, String teamLeaderId, int amount, Instant createdAt, Instant updatedAt) {
        this((Long) null, (Room) null, null, round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public RoomBid(int round, String teamLeaderId, int amount) {
        this(round, teamLeaderId, amount, Instant.now(), Instant.now());
    }

    private RoomBid(
        Long roomBidId,
        Room room,
        Long roomIdValue,
        int round,
        String teamLeaderId,
        int amount,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.persistentId = roomBidId;
        this.room = room;
        this.roomIdValue = roomIdValue;
        this.round = round;
        this.teamLeaderId = teamLeaderId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RoomBidId getId() {
        return this.persistentId == null ? null : new RoomBidId(this.persistentId);
    }

    public long getRoomBidId() {
        return this.persistentId == null ? 0L : this.persistentId;
    }

    public long getRoomId() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue == null ? 0L : this.roomIdValue;
    }

    public Room getRoom() {
        return room;
    }

    public RoomBid attach(Room room) {
        this.room = room;
        this.roomIdValue = room.getRoomId();
        return this;
    }

    public RoomBid detachCopy() {
        return new RoomBid(
            this.persistentId,
            this.roomIdOrNull(),
            this.round,
            this.teamLeaderId,
            this.amount,
            this.createdAt,
            this.updatedAt
        );
    }

    public RoomBid copy(
        long roomBidId,
        long roomId,
        int round,
        String teamLeaderId,
        int amount,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new RoomBid(roomBidId, roomId, round, teamLeaderId, amount, createdAt, updatedAt);
    }

    public RoomBid copy() {
        return detachCopy();
    }

    private Long roomIdOrNull() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue;
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
}
