package com.naminhyeok.fantazzk.room;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import org.jmolecules.ddd.types.ValueObject;

@Access(AccessType.FIELD)
@Embeddable
@EqualsAndHashCode
final class RoomTeamMember implements ValueObject {
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId teamLeaderId;
    @Column(name = "player_name")
    private String playerName;
    @Column(name = "assign_order")
    private int assignOrder;

    RoomTeamMember() {
    }

    RoomTeamMember(TeamLeaderId teamLeaderId, String playerName, int assignOrder) {
        this.teamLeaderId = teamLeaderId;
        this.playerName = playerName;
        this.assignOrder = assignOrder;
    }

    TeamLeaderId teamLeaderId() {
        return teamLeaderId;
    }

    String getTeamLeaderId() {
        return teamLeaderId.value();
    }
    String getPlayerName() {
        return playerName;
    }

    int getAssignOrder() {
        return assignOrder;
    }
}
