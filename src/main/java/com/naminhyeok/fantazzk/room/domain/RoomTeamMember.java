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
@Table(name = "room_team_member")
public class RoomTeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    private Long roomIdValue;

    @Column(name = "team_leader_id", nullable = false)
    private String teamLeaderId;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "assign_order", nullable = false)
    private int assignOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomTeamMember() {
        this((Long) null, (Room) null, null, "", "", 0, Instant.now(), Instant.now());
    }

    public RoomTeamMember(
        Long roomTeamMemberId,
        Long roomId,
        String teamLeaderId,
        String playerName,
        int assignOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomTeamMemberId != null && roomTeamMemberId != 0L ? roomTeamMemberId : null,
            null,
            roomId != null && roomId != 0L ? roomId : null,
            teamLeaderId,
            playerName,
            assignOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamMember(
        Long roomTeamMemberId,
        Long roomId,
        String teamLeaderId,
        String playerName,
        int assignOrder
    ) {
        this(roomTeamMemberId, roomId, teamLeaderId, playerName, assignOrder, Instant.now(), Instant.now());
    }

    public RoomTeamMember(
        RoomTeamMemberId roomTeamMemberId,
        RoomId roomId,
        String teamLeaderId,
        String playerName,
        int assignOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomTeamMemberId != null ? roomTeamMemberId.value() : null,
            roomId != null ? roomId.getValue() : null,
            teamLeaderId,
            playerName,
            assignOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamMember(
        Long roomId,
        String teamLeaderId,
        String playerName,
        int assignOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            null,
            roomId != null && roomId != 0L ? roomId : null,
            teamLeaderId,
            playerName,
            assignOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamMember(
        Long roomId,
        String teamLeaderId,
        String playerName,
        int assignOrder
    ) {
        this(
            null,
            roomId != null && roomId != 0L ? roomId : null,
            teamLeaderId,
            playerName,
            assignOrder,
            Instant.now(),
            Instant.now()
        );
    }

    public RoomTeamMember(
        String teamLeaderId,
        String playerName,
        int assignOrder
    ) {
        this((Long) null, teamLeaderId, playerName, assignOrder);
    }

    private RoomTeamMember(
        Long roomTeamMemberId,
        Room room,
        Long roomIdValue,
        String teamLeaderId,
        String playerName,
        int assignOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.persistentId = roomTeamMemberId;
        this.room = room;
        this.roomIdValue = roomIdValue;
        this.teamLeaderId = teamLeaderId;
        this.playerName = playerName;
        this.assignOrder = assignOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RoomTeamMemberId getId() {
        return this.persistentId == null ? null : new RoomTeamMemberId(this.persistentId);
    }

    public long getRoomTeamMemberId() {
        return this.persistentId == null ? 0L : this.persistentId;
    }

    public long getRoomId() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue == null ? 0L : this.roomIdValue;
    }

    public String getTeamLeaderId() {
        return teamLeaderId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getAssignOrder() {
        return assignOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    RoomTeamMember attach(Room room) {
        this.room = room;
        this.roomIdValue = room.getRoomId();
        return this;
    }

    public RoomTeamMember detachCopy() {
        return new RoomTeamMember(
            this.getRoomTeamMemberId(),
            roomIdOrNull(),
            teamLeaderId,
            playerName,
            assignOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamMember copy(
        long roomTeamMemberId,
        long roomId,
        String teamLeaderId,
        String playerName,
        int assignOrder,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new RoomTeamMember(
            roomTeamMemberId != 0L ? roomTeamMemberId : null,
            roomId,
            teamLeaderId,
            playerName,
            assignOrder,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamMember copy() {
        return detachCopy();
    }

    private Long roomIdOrNull() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue;
    }
}
