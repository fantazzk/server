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
    @Column(name = "player_id")
    @Convert(converter = RoomPlayerId.JpaConverter.class)
    private RoomPlayerId playerId;
    @Column(name = "assign_order")
    private int assignOrder;

    RoomTeamMember() {
    }

    RoomTeamMember(TeamLeaderId teamLeaderId, RoomPlayerId playerId, int assignOrder) {
        this.teamLeaderId = teamLeaderId;
        this.playerId = playerId;
        this.assignOrder = assignOrder;
    }

    TeamLeaderId teamLeaderId() {
        return teamLeaderId;
    }

    RoomPlayerId playerId() {
        return playerId;
    }

    int assignOrder() {
        return assignOrder;
    }
}
