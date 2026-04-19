package com.naminhyeok.fantazzk.room.domain;

public sealed interface GameParticipant permits AuctionParticipant, DraftParticipant {
    public TeamLeaderId teamLeaderId();

    public String nickname();

    public RoomMode mode();

    public default Integer draftPosition() {
        return null;
    }

    public default Integer remainingBudget() {
        return null;
    }

    public default AuctionState auctionState() {
        if (mode() != RoomMode.AUCTION) {
            throw RoomStateInvalidException.auctionWinnerBudgetMissing(teamLeaderId());
        }
        return new AuctionState(teamLeaderId(), nickname(), remainingBudget());
    }

    public default DraftState draftState() {
        if (mode() != RoomMode.DRAFT) {
            throw RoomStateInvalidException.draftPositionMissing(teamLeaderId());
        }
        return new DraftState(teamLeaderId(), nickname(), draftPosition());
    }

    public static AuctionParticipant auction(TeamLeaderId teamLeaderId, String nickname, int remainingBudget) {
        return new AuctionParticipant(teamLeaderId, nickname, remainingBudget);
    }

    public static DraftParticipant draft(TeamLeaderId teamLeaderId, String nickname, int draftPosition) {
        return new DraftParticipant(teamLeaderId, nickname, draftPosition);
    }

    public record AuctionState(
        TeamLeaderId teamLeaderId,
        String nickname,
        int remainingBudget
    ) {
    }

    public record DraftState(
        TeamLeaderId teamLeaderId,
        String nickname,
        int draftPosition
    ) {
    }
}
