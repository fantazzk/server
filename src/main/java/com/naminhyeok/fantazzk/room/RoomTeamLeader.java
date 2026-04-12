package com.naminhyeok.fantazzk.room;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Access(AccessType.FIELD)
@Embeddable
class RoomTeamLeader {
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId id;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "action_token")
    private String actionToken;
    @Column(name = "draft_position")
    private Integer draftPosition;
    @Column(name = "remaining_budget")
    private Integer remainingBudget;

    RoomTeamLeader() {
    }

    RoomTeamLeader(TeamLeaderId id, String nickname, String actionToken, Integer remainingBudget) {
        this.id = id;
        this.nickname = nickname;
        this.actionToken = actionToken;
        this.draftPosition = null;
        this.remainingBudget = remainingBudget;
    }

    public TeamLeaderId getId() {
        return id;
    }

    String getTeamLeaderId() {
        return id.value();
    }

    void spend(int amount) {
        if (remainingBudget == null) {
            throw RoomStateInvalidException.auctionWinnerBudgetMissing(id);
        }
        if (remainingBudget < amount) {
            throw RoomStateInvalidException.auctionWinnerBudgetExceeded(id, remainingBudget, amount);
        }
        remainingBudget -= amount;
    }

    void assignDraftPosition(int draftPosition) {
        this.draftPosition = draftPosition;
    }

    void clearDraftPosition() {
        this.draftPosition = null;
    }
}
