package com.naminhyeok.fantazzk.room.domain;

import com.naminhyeok.fantazzk.room.RoomId;
import java.time.Instant;
import java.util.Objects;
import org.springframework.lang.Nullable;

public final class RoomTeamLeader {
    private final RoomTeamLeaderId roomTeamLeaderId;
    private final RoomId roomId;
    private final String teamLeaderId;
    private final String nickname;
    private Integer remainingBudget;
    private final Instant createdAt;
    private final Instant updatedAt;

    private RoomTeamLeader(
            RoomTeamLeaderId roomTeamLeaderId,
            RoomId roomId,
            String teamLeaderId,
            String nickname,
            Integer remainingBudget,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.roomTeamLeaderId = roomTeamLeaderId == null ? RoomTeamLeaderId.random() : roomTeamLeaderId;
        this.roomId = Objects.requireNonNull(roomId, "roomId");
        this.teamLeaderId = requireText(teamLeaderId, "팀장 식별자는 비어 있을 수 없습니다");
        this.nickname = requireText(nickname, "팀장 닉네임은 비어 있을 수 없습니다");
        this.remainingBudget = remainingBudget;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static RoomTeamLeader create(RoomId roomId, String teamLeaderId, String nickname, Integer remainingBudget) {
        Instant now = Instant.now();
        return new RoomTeamLeader(null, roomId, teamLeaderId, nickname, remainingBudget, now, now);
    }

    public static RoomTeamLeader restore(
            RoomTeamLeaderId roomTeamLeaderId,
            RoomId roomId,
            String teamLeaderId,
            String nickname,
            Integer remainingBudget,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new RoomTeamLeader(roomTeamLeaderId, roomId, teamLeaderId, nickname, remainingBudget, createdAt, updatedAt);
    }

    public void requireCanBid(int amount) {
        budgetState().requireCanBid(amount);
    }

    public void validateBudget(int amount) {
        requireCanBid(amount);
    }

    public RoomTeamLeader spend(int amount) {
        remainingBudget = budgetState().spend(amount).getRemainingBudget();
        return this;
    }

    public RoomTeamLeader copy() {
        return restore(roomTeamLeaderId, roomId, teamLeaderId, nickname, remainingBudget, createdAt, updatedAt);
    }

    public RoomTeamLeaderId getRoomTeamLeaderId() {
        return roomTeamLeaderId;
    }

    public RoomId getRoomId() {
        return roomId;
    }

    public String getTeamLeaderId() {
        return teamLeaderId;
    }

    public String getNickname() {
        return nickname;
    }

    @Nullable
    public Integer getRemainingBudget() {
        return remainingBudget;
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
        if (!(other instanceof RoomTeamLeader roomTeamLeader)) {
            return false;
        }
        return roomTeamLeaderId.equals(roomTeamLeader.roomTeamLeaderId)
                && roomId.equals(roomTeamLeader.roomId)
                && teamLeaderId.equals(roomTeamLeader.teamLeaderId)
                && nickname.equals(roomTeamLeader.nickname)
                && Objects.equals(remainingBudget, roomTeamLeader.remainingBudget)
                && createdAt.equals(roomTeamLeader.createdAt)
                && updatedAt.equals(roomTeamLeader.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTeamLeaderId, roomId, teamLeaderId, nickname, remainingBudget, createdAt, updatedAt);
    }

    private BudgetState budgetState() {
        return BudgetState.Companion.requireFrom(remainingBudget);
    }

    private static String requireText(String value, String message) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
