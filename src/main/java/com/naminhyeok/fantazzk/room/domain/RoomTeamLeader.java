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
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "room_team_leader")
public class RoomTeamLeader {
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

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "remaining_budget")
    private Integer remainingBudget;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomTeamLeader() {
        this((Long) null, (Room) null, null, "", "", null, Instant.now(), Instant.now());
    }

    public RoomTeamLeader(
        Long roomTeamLeaderId,
        Long roomId,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomTeamLeaderId != null && roomTeamLeaderId != 0L ? roomTeamLeaderId : null,
            null,
            roomId != null && roomId != 0L ? roomId : null,
            teamLeaderId,
            nickname,
            remainingBudget,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamLeader(
        Long roomTeamLeaderId,
        Long roomId,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget
    ) {
        this(roomTeamLeaderId, roomId, teamLeaderId, nickname, remainingBudget, Instant.now(), Instant.now());
    }

    public RoomTeamLeader(
        Long roomTeamLeaderId,
        Long roomId,
        String teamLeaderId,
        String nickname
    ) {
        this(roomTeamLeaderId, roomId, teamLeaderId, nickname, null, Instant.now(), Instant.now());
    }

    public RoomTeamLeader(
        RoomTeamLeaderId roomTeamLeaderId,
        RoomId roomId,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            roomTeamLeaderId != null ? roomTeamLeaderId.value() : null,
            roomId != null ? roomId.getValue() : null,
            teamLeaderId,
            nickname,
            remainingBudget,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamLeader(
        Long roomId,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(
            null,
            roomId != null && roomId != 0L ? roomId : null,
            teamLeaderId,
            nickname,
            remainingBudget,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamLeader(
        Long roomId,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget
    ) {
        this(
            null,
            roomId != null && roomId != 0L ? roomId : null,
            teamLeaderId,
            nickname,
            remainingBudget,
            Instant.now(),
            Instant.now()
        );
    }

    public RoomTeamLeader(
        String teamLeaderId,
        String nickname
    ) {
        this(
            (Long) null,
            (Long) null,
            teamLeaderId,
            nickname,
            null,
            Instant.now(),
            Instant.now()
        );
    }

    public RoomTeamLeader(
        String teamLeaderId,
        String nickname,
        Integer remainingBudget
    ) {
        this(
            (Long) null,
            (Long) null,
            teamLeaderId,
            nickname,
            remainingBudget,
            Instant.now(),
            Instant.now()
        );
    }

    private RoomTeamLeader(
        Long roomTeamLeaderId,
        Room room,
        Long roomIdValue,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.persistentId = roomTeamLeaderId;
        this.room = room;
        this.roomIdValue = roomIdValue;
        this.teamLeaderId = teamLeaderId;
        this.nickname = nickname;
        this.remainingBudget = remainingBudget;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RoomTeamLeaderId getId() {
        return this.persistentId == null ? null : new RoomTeamLeaderId(this.persistentId);
    }

    public long getRoomTeamLeaderId() {
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

    public String getNickname() {
        return nickname;
    }

    @Nullable
    public Integer getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(Integer remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void requireCanBid(int amount) {
        budgetState().requireCanBid(amount);
    }

    public void validateBudget(int amount) {
        requireCanBid(amount);
    }

    public RoomTeamLeader spend(int amount) {
        this.remainingBudget = budgetState().spend(amount).remainingBudget();
        return this;
    }

    private BudgetState budgetState() {
        return BudgetState.requireFrom(remainingBudget);
    }

    RoomTeamLeader attach(Room room) {
        this.room = room;
        this.roomIdValue = room.getRoomId();
        return this;
    }

    public RoomTeamLeader detachCopy() {
        return new RoomTeamLeader(
            this.getRoomTeamLeaderId(),
            roomIdOrNull(),
            teamLeaderId,
            nickname,
            remainingBudget,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamLeader copy(
        long roomTeamLeaderId,
        long roomId,
        String teamLeaderId,
        String nickname,
        Integer remainingBudget,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new RoomTeamLeader(
            roomTeamLeaderId != 0L ? roomTeamLeaderId : null,
            roomId,
            teamLeaderId,
            nickname,
            remainingBudget,
            createdAt,
            updatedAt
        );
    }

    public RoomTeamLeader copy() {
        return detachCopy();
    }

    private Long roomIdOrNull() {
        if (this.room != null) {
            return this.room.getRoomId();
        }
        return this.roomIdValue;
    }
}
