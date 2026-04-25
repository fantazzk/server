package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.types.ValueObject;

@Access(AccessType.FIELD)
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public final class AuctionParticipant implements GameParticipant, ValueObject {
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId teamLeaderId;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "remaining_budget")
    private Integer remainingBudget;

    public AuctionParticipant(TeamLeaderId teamLeaderId, String nickname, int remainingBudget) {
        this.teamLeaderId = java.util.Objects.requireNonNull(teamLeaderId, "teamLeaderId must not be null");
        this.nickname = java.util.Objects.requireNonNull(nickname, "nickname must not be null");
        this.remainingBudget = remainingBudget;
    }

    @Override
    public TeamLeaderId teamLeaderId() {
        return teamLeaderId;
    }

    @Override
    public String nickname() {
        return nickname;
    }

    @Override
    public RoomMode mode() {
        return RoomMode.AUCTION;
    }

    @Override
    public Integer remainingBudget() {
        return remainingBudget;
    }

    public AuctionParticipant withRemainingBudget(int remainingBudget) {
        return new AuctionParticipant(teamLeaderId, nickname, remainingBudget);
    }
}
