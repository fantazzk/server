package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.naminhyeok.fantazzk.room.application.port.AuctionDeadlineSettlementProcessor;
import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleCandidate;
import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleReader;
import com.naminhyeok.fantazzk.room.infrastructure.scheduling.RoomAuctionDeadlineScheduler;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class RoomAuctionDeadlineSchedulerTest {
    private static final Instant NOW = Instant.parse("2026-04-09T00:00:10Z");

    @Test
    void schedule는_경매_room_deadline에_맞춰_정산_task를_등록하고_다음_deadline을_재예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        AuctionDeadlineSettlementProcessor processor = mock(AuctionDeadlineSettlementProcessor.class);
        RecordingSchedules schedules = new RecordingSchedules(List.of(schedule("ROOM01", Instant.parse("2026-04-09T00:00:30Z"))));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, processor, schedules, Clock.fixed(NOW, ZoneOffset.UTC));

        scheduler.schedule("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));

        assertThat(taskScheduler.scheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));

        taskScheduler.runLatest();

        verify(processor).processDueAuction("ROOM01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
    }

    @Test
    void schedule는_같은_room의_기존_deadline_예약을_취소하고_새_deadline만_남긴다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, code -> {}, new RecordingSchedules(), Clock.fixed(NOW, ZoneOffset.UTC));

        scheduler.schedule("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        scheduler.schedule("ROOM01", Instant.parse("2026-04-09T00:00:20Z"));

        assertThat(taskScheduler.cancelledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:20Z"));
    }

    @Test
    void 애플리케이션_시작시_due_room은_즉시_catch_up하고_future_deadline은_재예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        AuctionDeadlineSettlementProcessor processor = mock(AuctionDeadlineSettlementProcessor.class);
        AuctionScheduleReader scheduleReader =
            new RecordingSchedules(
                List.of(
                    schedule("DUE01", Instant.parse("2026-04-09T00:00:05Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                ),
                List.of(schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z")))
            );
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, processor, scheduleReader, Clock.fixed(NOW, ZoneOffset.UTC));

        scheduler.catchUpAndReschedule();

        verify(processor).processDueAuction("DUE01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
    }

    @Test
    void 애플리케이션_시작시_손상된_due_room이_있어도_뒤따르는_정상_room은_계속_처리한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        AuctionDeadlineSettlementProcessor processor = mock(AuctionDeadlineSettlementProcessor.class);
        AuctionScheduleReader scheduleReader =
            new RecordingSchedules(
                List.of(
                    schedule("BROKEN01", Instant.parse("2026-04-09T00:00:05Z")),
                    schedule("DUE02", Instant.parse("2026-04-09T00:00:06Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                ),
                List.of(
                    schedule("DUE02", Instant.parse("2026-04-09T00:00:25Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                )
            );
        willThrow(RoomStateInvalidException.auctionRoundMissing()).given(processor).processDueAuction("BROKEN01");
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, processor, scheduleReader, Clock.fixed(NOW, ZoneOffset.UTC));

        scheduler.catchUpAndReschedule();

        verify(processor).processDueAuction("BROKEN01");
        verify(processor).processDueAuction("DUE02");
        assertThat(taskScheduler.activeScheduledInstants())
            .containsExactly(
                Instant.parse("2026-04-09T00:00:25Z"),
                Instant.parse("2026-04-09T00:00:15Z")
            );
    }

    @Test
    void 애플리케이션_시작시_schedulable_room은_application_layer_정렬규칙으로_처리한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        AuctionDeadlineSettlementProcessor processor = mock(AuctionDeadlineSettlementProcessor.class);
        AuctionScheduleReader scheduleReader =
            new RecordingSchedules(
                List.of(
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z")),
                    schedule("DUE01", Instant.parse("2026-04-09T00:00:05Z")),
                    schedule("LEGACY01", Instant.parse("2026-04-09T00:00:01Z"))
                ),
                List.of(
                    schedule("LEGACY01", Instant.parse("2026-04-09T00:00:20Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                ),
                List.of(
                    schedule("LEGACY01", Instant.parse("2026-04-09T00:00:20Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                )
            );
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, processor, scheduleReader, Clock.fixed(NOW, ZoneOffset.UTC));

        scheduler.catchUpAndReschedule();

        InOrder inOrder = inOrder(processor);
        inOrder.verify(processor).processDueAuction("LEGACY01");
        inOrder.verify(processor).processDueAuction("DUE01");
        assertThat(taskScheduler.activeScheduledInstants())
            .containsExactly(
                Instant.parse("2026-04-09T00:00:20Z"),
                Instant.parse("2026-04-09T00:00:15Z")
            );
    }

    @Test
    void 애플리케이션_시작시_첫_페이지를_넘는_future_deadline도_모두_재예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        AuctionScheduleReader scheduleReader = new RecordingSchedules(manyFutureSchedules(205));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(taskScheduler, code -> {}, scheduleReader, Clock.fixed(NOW, ZoneOffset.UTC));

        scheduler.catchUpAndReschedule();

        assertThat(taskScheduler.activeScheduledInstants()).hasSize(205);
        assertThat(taskScheduler.activeScheduledInstants()).contains(
            Instant.parse("2026-04-09T00:01:00Z"),
            Instant.parse("2026-04-09T00:04:24Z")
        );
    }

    private static AuctionScheduleCandidate schedule(String code, Instant deadline) {
        return new AuctionScheduleCandidate(code, deadline);
    }

    private static List<AuctionScheduleCandidate> manyFutureSchedules(int count) {
        List<AuctionScheduleCandidate> candidates = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            int second = 60 + index;
            candidates.add(
                schedule(
                    "ROOM%03d".formatted(index),
                    Instant.parse("2026-04-09T00:%02d:%02dZ".formatted(second / 60, second % 60))
                )
            );
        }
        return candidates;
    }

    private static final class RecordingSchedules implements AuctionScheduleReader {
        private final Queue<List<AuctionScheduleCandidate>> responses = new ArrayDeque<>();
        private List<AuctionScheduleCandidate> lastResponse = List.of();

        private RecordingSchedules(List<AuctionScheduleCandidate>... responses) {
            this.responses.addAll(List.of(responses));
            if (!this.responses.isEmpty()) {
                lastResponse = this.responses.peek();
            }
        }

        @Override
        public List<AuctionScheduleCandidate> findInProgressAuctionSchedules() {
            List<AuctionScheduleCandidate> response = responses.poll();
            if (response != null) {
                lastResponse = response;
            }
            return lastResponse;
        }
    }
}
