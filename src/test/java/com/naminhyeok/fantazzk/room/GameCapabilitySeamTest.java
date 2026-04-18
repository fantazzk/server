package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GameCapabilitySeamTest {
    @Test
    void 경매_규칙은_auction_seam으로_드러난다() {
        GameRules rules = GameRules.auction(2, 2, 300, 45, 10, 1);

        assertThat(rules.mode()).isEqualTo(RoomMode.AUCTION);
        assertThat(rules.auctionRules())
            .extracting(
                GameRules.AuctionRules::budget,
                GameRules.AuctionRules::pickBanTime,
                GameRules.AuctionRules::minBidUnit,
                GameRules.AuctionRules::positionLimit
            )
            .containsExactly(300, 45, 10, 1);
        assertThatThrownBy(rules::draftRules).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 드래프트_규칙은_draft_seam으로_드러난다() {
        GameRules rules = GameRules.draft(2, 2, 30, DraftOrderStrategy.SNAKE);

        assertThat(rules.mode()).isEqualTo(RoomMode.DRAFT);
        assertThat(rules.draftRules())
            .extracting(
                GameRules.DraftRules::pickBanTime,
                GameRules.DraftRules::draftOrderStrategy
            )
            .containsExactly(30, DraftOrderStrategy.SNAKE);
        assertThatThrownBy(rules::auctionRules).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 경매_참가자는_auction_state로_예산을_드러낸다() {
        GameParticipant participant = GameParticipant.auction(new TeamLeaderId("host-1"), "호스트", 300);

        assertThat(participant.mode()).isEqualTo(RoomMode.AUCTION);
        assertThat(participant.auctionState())
            .extracting(
                GameParticipant.AuctionState::teamLeaderId,
                GameParticipant.AuctionState::nickname,
                GameParticipant.AuctionState::remainingBudget
            )
            .containsExactly(new TeamLeaderId("host-1"), "호스트", 300);
        assertThatThrownBy(participant::draftState).isInstanceOf(RoomStateInvalidException.class);
    }

    @Test
    void 드래프트_참가자는_draft_state로_순서를_드러낸다() {
        GameParticipant participant = GameParticipant.draft(new TeamLeaderId("guest-1"), "게스트", 2);

        assertThat(participant.mode()).isEqualTo(RoomMode.DRAFT);
        assertThat(participant.draftState())
            .extracting(
                GameParticipant.DraftState::teamLeaderId,
                GameParticipant.DraftState::nickname,
                GameParticipant.DraftState::draftPosition
            )
            .containsExactly(new TeamLeaderId("guest-1"), "게스트", 2);
        assertThatThrownBy(participant::auctionState).isInstanceOf(RoomStateInvalidException.class);
    }
}
