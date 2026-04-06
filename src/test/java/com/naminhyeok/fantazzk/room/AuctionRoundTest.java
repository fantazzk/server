package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.RoomId;
import com.naminhyeok.fantazzk.room.domain.AuctionOutcome;
import com.naminhyeok.fantazzk.room.domain.AuctionRound;
import com.naminhyeok.fantazzk.room.domain.AuctionRoundSettlement;
import com.naminhyeok.fantazzk.room.domain.RoomBid;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuctionRoundTest {

    @Test
    void higher_bid_must_exceed_the_current_highest_bid() {
        RoomBid highestBid = RoomBid.restore(
                null,
                RoomId.random(),
                3,
                "leader-A",
                100,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"));
        AuctionRound round = new AuctionRound(3, highestBid);

        assertThatThrownBy(() -> round.requireHigherBid(100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 최고가보다 높아야 합니다");
        assertThatCode(() -> round.requireHigherBid(101)).doesNotThrowAnyException();
    }

    @Test
    void settlement_is_sold_when_a_highest_bid_exists() {
        RoomBid highestBid = RoomBid.restore(
                null,
                RoomId.random(),
                2,
                "leader-B",
                150,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"));
        AuctionRound round = new AuctionRound(2, highestBid);

        AuctionRoundSettlement settlement = round.settle("선수1", 4, 4);

        assertThat(settlement.getOutcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.getNextRound()).isEqualTo(3);
        assertThat(settlement.isCompleted()).isTrue();
        assertThat(settlement.getWinningBid()).isEqualTo(highestBid);
    }

    @Test
    void settlement_is_passed_when_there_is_no_bid_and_full_roster_is_rejected() {
        AuctionRound round = new AuctionRound(1, null);

        AuctionRoundSettlement settlement = round.settle("선수2", 1, 4);

        assertThat(settlement.getOutcome()).isEqualTo(AuctionOutcome.PASSED);
        assertThat(settlement.isCompleted()).isFalse();
        assertThatThrownBy(() -> new AuctionRound(3, null).requireRosterCapacity(2, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("팀장의 팀원 정원이 가득 찼습니다");
    }
}
