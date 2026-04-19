package com.naminhyeok.fantazzk.room.domain;

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
public class AuctionBid implements ValueObject {
    @Column(name = "round")
    private int round;
    @Column(name = "bid_sequence")
    @Convert(converter = BidSequence.JpaConverter.class)
    private BidSequence sequence;
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId teamLeaderId;
    @Column(name = "amount")
    private int amount;

    public AuctionBid() {
    }

    public AuctionBid(int round, BidSequence sequence, TeamLeaderId teamLeaderId, int amount) {
        this.round = round;
        this.sequence = sequence;
        this.teamLeaderId = teamLeaderId;
        this.amount = amount;
    }

    public int round() {
        return round;
    }

    public BidSequence sequence() {
        return sequence;
    }

    public TeamLeaderId teamLeaderId() {
        return teamLeaderId;
    }

    public int amount() {
        return amount;
    }
}
