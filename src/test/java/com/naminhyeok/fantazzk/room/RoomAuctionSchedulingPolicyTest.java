package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.event.AuctionSettled;
import com.naminhyeok.fantazzk.room.event.BidPlaced;
import com.naminhyeok.fantazzk.room.event.RoomStarted;
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
            new RoomAuctionDeadlineScheduler(taskScheduler, null, List::of, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), emptyGames())
        );

        policy.on(new RoomStarted("ROOM01", Instant.parse("2026-04-09T00:00:15Z")));

        assertThat(taskScheduler.scheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
    }

    @Test
    void bidPlaced는_deadline을_갱신한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, null, List::of, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), emptyGames());
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
            new RoomAuctionDeadlineScheduler(taskScheduler, null, List::of, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), emptyGames());
        scheduler.refresh("ROOM01", Instant.parse("2026-04-09T00:00:30Z"));
        RoomAuctionSchedulingPolicy policy = new RoomAuctionSchedulingPolicy(scheduler);

        policy.on(new AuctionSettled("ROOM01", AuctionOutcome.SOLD.name(), null));

        assertThat(taskScheduler.cancelledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
        assertThat(taskScheduler.activeScheduledInstants()).isEmpty();
    }

    private static Games emptyGames() {
        return new Games() {
            @Override
            public Game save(Game game) {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.util.Optional<Game> findById(GameId id) {
                return java.util.Optional.empty();
            }
        };
    }
}
