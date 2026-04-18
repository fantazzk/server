package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.support.AuctionSettled;
import com.naminhyeok.fantazzk.room.application.support.BidPlaced;
import com.naminhyeok.fantazzk.room.application.support.RoomStarted;
import com.naminhyeok.fantazzk.room.infrastructure.scheduling.RoomAuctionDeadlineScheduler;
import com.naminhyeok.fantazzk.room.infrastructure.scheduling.RoomAuctionSchedulingPolicy;
import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomAuctionSchedulingPolicyTest {
    @Test
    void roomStarted는_deadline을_예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RoomAuctionSchedulingPolicy policy = new RoomAuctionSchedulingPolicy(
            new RoomAuctionDeadlineScheduler(taskScheduler, code -> {}, List::of, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
        );

        policy.on(new RoomStarted("ROOM01", Instant.parse("2026-04-09T00:00:15Z")));

        assertThat(taskScheduler.scheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
    }

    @Test
    void bidPlaced는_deadline을_갱신한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, code -> {}, List::of, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        scheduler.refresh("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        RoomAuctionSchedulingPolicy policy = new RoomAuctionSchedulingPolicy(scheduler);

        policy.on(new BidPlaced("ROOM01", "leader-1", 150, 1, Instant.parse("2026-04-09T00:00:30Z")));

        assertThat(taskScheduler.cancelledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
    }

    @Test
    void auctionSettled는_다음_deadline이_없으면_예약을_해제한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, code -> {}, List::of, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        scheduler.refresh("ROOM01", Instant.parse("2026-04-09T00:00:30Z"));
        RoomAuctionSchedulingPolicy policy = new RoomAuctionSchedulingPolicy(scheduler);

        policy.on(new AuctionSettled("ROOM01", AuctionOutcome.SOLD.name(), null));

        assertThat(taskScheduler.cancelledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
        assertThat(taskScheduler.activeScheduledInstants()).isEmpty();
    }
}
