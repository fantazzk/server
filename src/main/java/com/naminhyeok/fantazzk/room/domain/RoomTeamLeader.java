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
@Table(name = "room_team_leader")
public class RoomTeamLeader {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID persistentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "team_leader_id", nullable = false, length = 36)
    private String teamLeaderId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "remaining_budget")
    private Integer remainingBudget;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomTeamLeader() {
        this(null, Room.reference(RoomId.random()), "leader", "nickname", null, Instant.now(), Instant.now());
    }

    private RoomTeamLeader(
            @Nullable RoomTeamLeaderId roomTeamLeaderId,
            Room room,
            String teamLeaderId,
            String nickname,
            Integer remainingBudget,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.persistentId = roomTeamLeaderId == null ? UUID.randomUUID() : roomTeamLeaderId.getValue();
        this.room = Objects.requireNonNull(room, "room");
        this.teamLeaderId = requireText(teamLeaderId, "팀장 식별자는 비어 있을 수 없습니다");
        this.nickname = requireText(nickname, "팀장 닉네임은 비어 있을 수 없습니다");
        this.remainingBudget = remainingBudget;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    private RoomTeamLeader(
            @Nullable RoomTeamLeaderId roomTeamLeaderId,
            RoomId roomId,
            String teamLeaderId,
            String nickname,
            Integer remainingBudget,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(roomTeamLeaderId, Room.reference(roomId), teamLeaderId, nickname, remainingBudget, createdAt, updatedAt);
    }

    public static RoomTeamLeader create(RoomId roomId, String teamLeaderId, String nickname, Integer remainingBudget) {
        Instant now = Instant.now();
        return new RoomTeamLeader(null, roomId, teamLeaderId, nickname, remainingBudget, now, now);
    }

    public static RoomTeamLeader restore(
            @Nullable RoomTeamLeaderId roomTeamLeaderId,
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
        return restore(getRoomTeamLeaderId(), getRoomId(), teamLeaderId, nickname, remainingBudget, createdAt, updatedAt);
    }

    public RoomTeamLeaderId getRoomTeamLeaderId() {
        return RoomTeamLeaderId.from(Objects.requireNonNull(persistentId, "roomTeamLeaderId is not assigned"));
    }

    public RoomId getRoomId() {
        return room.getRoomId();
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

    void attach(Room room) {
        this.room = Objects.requireNonNull(room, "room");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomTeamLeader roomTeamLeader)) {
            return false;
        }
        return getRoomTeamLeaderId().equals(roomTeamLeader.getRoomTeamLeaderId())
                && getRoomId().equals(roomTeamLeader.getRoomId())
                && teamLeaderId.equals(roomTeamLeader.teamLeaderId)
                && nickname.equals(roomTeamLeader.nickname)
                && Objects.equals(remainingBudget, roomTeamLeader.remainingBudget)
                && createdAt.equals(roomTeamLeader.createdAt)
                && updatedAt.equals(roomTeamLeader.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoomTeamLeaderId(), getRoomId(), teamLeaderId, nickname, remainingBudget, createdAt, updatedAt);
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
