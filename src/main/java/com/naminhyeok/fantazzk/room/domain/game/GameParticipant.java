package com.naminhyeok.fantazzk.room.domain.game;

import com.naminhyeok.fantazzk.room.domain.event.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public sealed interface GameParticipant permits AuctionParticipant, DraftParticipant {
    TeamLeaderId teamLeaderId();

    String nickname();

    RoomMode mode();

    default Integer draftPosition() {
        return null;
    }

    default Integer remainingBudget() {
        return null;
    }

    default AuctionState auctionState() {
        if (mode() != RoomMode.AUCTION) {
            throw RoomStateInvalidException.auctionWinnerBudgetMissing(teamLeaderId());
        }
        return new AuctionState(teamLeaderId(), nickname(), remainingBudget());
    }

    default DraftState draftState() {
        if (mode() != RoomMode.DRAFT) {
            throw RoomStateInvalidException.draftPositionMissing(teamLeaderId());
        }
        return new DraftState(teamLeaderId(), nickname(), draftPosition());
    }

    static AuctionParticipant auction(TeamLeaderId teamLeaderId, String nickname, int remainingBudget) {
        return new AuctionParticipant(teamLeaderId, nickname, remainingBudget);
    }

    static DraftParticipant draft(TeamLeaderId teamLeaderId, String nickname, int draftPosition) {
        return new DraftParticipant(teamLeaderId, nickname, draftPosition);
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
