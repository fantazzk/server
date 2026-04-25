package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Access(AccessType.FIELD)
@Embeddable
public class RoomTeamLeader {
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

    public RoomTeamLeader(TeamLeaderId id, String nickname, String actionToken, Integer remainingBudget) {
        this.id = id;
        this.nickname = nickname;
        this.actionToken = actionToken;
        this.draftPosition = null;
        this.remainingBudget = remainingBudget;
    }

    public void assignDraftPosition(int draftPosition) {
        this.draftPosition = draftPosition;
    }

    public void clearDraftPosition() {
        this.draftPosition = null;
    }
}
