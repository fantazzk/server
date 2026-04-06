package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomId;
import java.time.Instant;
import java.util.Objects;

public final class RoomTeamMember {
    private final RoomTeamMemberId roomTeamMemberId;
    private final RoomId roomId;
    private final String teamLeaderId;
    private final String playerName;
    private final int assignOrder;
    private final Instant createdAt;
    private final Instant updatedAt;

    private RoomTeamMember(
            RoomTeamMemberId roomTeamMemberId,
            RoomId roomId,
            String teamLeaderId,
            String playerName,
            int assignOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.roomTeamMemberId = roomTeamMemberId == null ? RoomTeamMemberId.random() : roomTeamMemberId;
        this.roomId = Objects.requireNonNull(roomId, "roomId");
        this.teamLeaderId = requireText(teamLeaderId, "팀장 식별자는 비어 있을 수 없습니다");
        this.playerName = requireText(playerName, "선수 이름은 비어 있을 수 없습니다");
        this.assignOrder = assignOrder;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static RoomTeamMember create(RoomId roomId, String teamLeaderId, String playerName, int assignOrder) {
        Instant now = Instant.now();
        return new RoomTeamMember(null, roomId, teamLeaderId, playerName, assignOrder, now, now);
    }

    public static RoomTeamMember restore(
            RoomTeamMemberId roomTeamMemberId,
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
        return restore(roomTeamMemberId, roomId, teamLeaderId, playerName, assignOrder, createdAt, updatedAt);
    }

    public RoomTeamMemberId getRoomTeamMemberId() {
        return roomTeamMemberId;
    }

    public RoomId getRoomId() {
        return roomId;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomTeamMember roomTeamMember)) {
            return false;
        }
        return assignOrder == roomTeamMember.assignOrder
                && roomTeamMemberId.equals(roomTeamMember.roomTeamMemberId)
                && roomId.equals(roomTeamMember.roomId)
                && teamLeaderId.equals(roomTeamMember.teamLeaderId)
                && playerName.equals(roomTeamMember.playerName)
                && createdAt.equals(roomTeamMember.createdAt)
                && updatedAt.equals(roomTeamMember.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTeamMemberId, roomId, teamLeaderId, playerName, assignOrder, createdAt, updatedAt);
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
