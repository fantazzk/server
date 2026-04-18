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
final class GameParticipant implements ValueObject {
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId teamLeaderId;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "draft_position")
    private Integer draftPosition;
    @Column(name = "remaining_budget")
    private Integer remainingBudget;

    GameParticipant() {
    }

    GameParticipant(TeamLeaderId teamLeaderId, String nickname, Integer draftPosition, Integer remainingBudget) {
        this.teamLeaderId = java.util.Objects.requireNonNull(teamLeaderId, "teamLeaderId must not be null");
        this.nickname = java.util.Objects.requireNonNull(nickname, "nickname must not be null");
        if ((draftPosition == null) == (remainingBudget == null)) {
            throw new IllegalArgumentException("game participant는 draftPosition 또는 remainingBudget 중 하나만 가져야 합니다");
        }
        this.draftPosition = draftPosition;
        this.remainingBudget = remainingBudget;
    }

    static GameParticipant auction(TeamLeaderId teamLeaderId, String nickname, int remainingBudget) {
        return new GameParticipant(teamLeaderId, nickname, null, remainingBudget);
    }

    static GameParticipant draft(TeamLeaderId teamLeaderId, String nickname, int draftPosition) {
        return new GameParticipant(teamLeaderId, nickname, draftPosition, null);
    }

    TeamLeaderId teamLeaderId() {
        return teamLeaderId;
    }

    String nickname() {
        return nickname;
    }

    Integer draftPosition() {
        return draftPosition;
    }

    Integer remainingBudget() {
        return remainingBudget;
    }

    RoomMode mode() {
        return remainingBudget != null ? RoomMode.AUCTION : RoomMode.DRAFT;
    }

    AuctionState auctionState() {
        if (mode() != RoomMode.AUCTION) {
            throw RoomStateInvalidException.auctionWinnerBudgetMissing(teamLeaderId);
        }
        return new AuctionState(teamLeaderId, nickname, remainingBudget);
    }

    DraftState draftState() {
        if (mode() != RoomMode.DRAFT) {
            throw RoomStateInvalidException.draftPositionMissing(teamLeaderId);
        }
        return new DraftState(teamLeaderId, nickname, draftPosition);
    }

    GameParticipant withRemainingBudget(int remainingBudget) {
        return new GameParticipant(teamLeaderId, nickname, draftPosition, remainingBudget);
    }

    record AuctionState(
        TeamLeaderId teamLeaderId,
        String nickname,
        int remainingBudget
    ) {
    }

    record DraftState(
        TeamLeaderId teamLeaderId,
        String nickname,
        int draftPosition
    ) {
    }
}
