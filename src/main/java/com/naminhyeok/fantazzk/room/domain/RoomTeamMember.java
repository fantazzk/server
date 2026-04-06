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
@Table(name = "room_team_member")
public class RoomTeamMember {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "team_leader_id", nullable = false, length = 36)
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
        this(null, Room.reference(RoomId.random()), "leader", "player", 0, Instant.now(), Instant.now());
    }

    private RoomTeamMember(
            @Nullable RoomTeamMemberId roomTeamMemberId,
            Room room,
            String teamLeaderId,
            String playerName,
            int assignOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.persistentId = roomTeamMemberId == null ? UUID.randomUUID() : roomTeamMemberId.getValue();
        this.room = Objects.requireNonNull(room, "room");
        this.teamLeaderId = requireText(teamLeaderId, "팀장 식별자는 비어 있을 수 없습니다");
        this.playerName = requireText(playerName, "선수 이름은 비어 있을 수 없습니다");
        this.assignOrder = assignOrder;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    private RoomTeamMember(
            @Nullable RoomTeamMemberId roomTeamMemberId,
            RoomId roomId,
            String teamLeaderId,
            String playerName,
            int assignOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(roomTeamMemberId, Room.reference(roomId), teamLeaderId, playerName, assignOrder, createdAt, updatedAt);
    }

    public static RoomTeamMember create(RoomId roomId, String teamLeaderId, String playerName, int assignOrder) {
        Instant now = Instant.now();
        return new RoomTeamMember(null, roomId, teamLeaderId, playerName, assignOrder, now, now);
    }

    public static RoomTeamMember restore(
            @Nullable RoomTeamMemberId roomTeamMemberId,
            RoomId roomId,
            String teamLeaderId,
            String playerName,
            int assignOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new RoomTeamMember(roomTeamMemberId, roomId, teamLeaderId, playerName, assignOrder, createdAt, updatedAt);
    }

    public RoomTeamMember copy() {
        return restore(getRoomTeamMemberId(), getRoomId(), teamLeaderId, playerName, assignOrder, createdAt, updatedAt);
    }

    public RoomTeamMemberId getRoomTeamMemberId() {
        return RoomTeamMemberId.from(Objects.requireNonNull(persistentId, "roomTeamMemberId is not assigned"));
    }

    public RoomId getRoomId() {
        return room.getRoomId();
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

    void attach(Room room) {
        this.room = Objects.requireNonNull(room, "room");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomTeamMember roomTeamMember)) {
            return false;
        }
        return assignOrder == roomTeamMember.assignOrder
                && getRoomTeamMemberId().equals(roomTeamMember.getRoomTeamMemberId())
                && getRoomId().equals(roomTeamMember.getRoomId())
                && teamLeaderId.equals(roomTeamMember.teamLeaderId)
                && playerName.equals(roomTeamMember.playerName)
                && createdAt.equals(roomTeamMember.createdAt)
                && updatedAt.equals(roomTeamMember.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoomTeamMemberId(), getRoomId(), teamLeaderId, playerName, assignOrder, createdAt, updatedAt);
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
