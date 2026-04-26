package com.naminhyeok.fantazzk.room.infrastructure.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.naminhyeok.fantazzk.room.application.SettleAuction;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomStateInvalidException;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.infrastructure.persistence.AuctionScheduleJpaRepository;
import com.naminhyeok.fantazzk.room.infrastructure.persistence.JpaAuctionScheduleReader;
import com.naminhyeok.fantazzk.room.infrastructure.schedule.RoomAuctionDeadlineScheduler;
import com.naminhyeok.fantazzk.room.repository.Games;
import org.springframework.data.domain.Pageable;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class RoomAuctionDeadlineSchedulerTest {
    private static final Instant NOW = Instant.parse("2026-04-09T00:00:10Z");

    @Test
    void 경매_마감_예약은_정산_후_다음_라운드_마감으로_이어진다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        StartedAuctionContext room = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        StartedAuctionContext nextRoundRoom = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:30Z"));
        given(settleAuction.settleIfDue("ROOM01")).willReturn(nextRoundRoom.room());
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                new JpaAuctionScheduleReader(new RecordingScheduleRepository()),
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames(room.game(), nextRoundRoom.game())
            );

        scheduler.schedule(room.room());

        assertThat(taskScheduler.scheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));

        taskScheduler.runLatest();

        verify(settleAuction).settleIfDue("ROOM01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
    }

    @Test
    void 같은_방의_마감은_하나만_활성화된다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        StartedAuctionContext first = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        StartedAuctionContext second = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:20Z"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                new JpaAuctionScheduleReader(new RecordingScheduleRepository()),
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames(first.game(), second.game())
            );

        scheduler.schedule(first.room());
        scheduler.schedule(second.room());

        assertThat(taskScheduler.cancelledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:20Z"));
    }

    @Test
    void 재시작_시점에_지난_마감은_즉시_정산하고_미래_마감은_다시_예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        JpaAuctionScheduleReader scheduleReader =
            new JpaAuctionScheduleReader(new RecordingScheduleRepository(
                schedule("DUE01", Instant.parse("2026-04-09T00:00:05Z")),
                schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
            ));
        given(settleAuction.settleIfDue("DUE01")).willReturn(completedAuctionRoom("DUE01"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                scheduleReader,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames()
            );

        scheduler.catchUpAndReschedule();

        verify(settleAuction).settleIfDue("DUE01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
    }

    @Test
    void 재시작_정산_중_손상된_방이_있어도_다음_방은_계속_처리한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        JpaAuctionScheduleReader scheduleReader =
            new JpaAuctionScheduleReader(new RecordingScheduleRepository(
                List.of(
                    schedule("BROKEN01", Instant.parse("2026-04-09T00:00:05Z")),
                    schedule("DUE02", Instant.parse("2026-04-09T00:00:06Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                )
            ));
        StartedAuctionContext due02 = auctionRoomWithDeadline("DUE02", Instant.parse("2026-04-09T00:00:25Z"));
        given(settleAuction.settleIfDue("BROKEN01")).willThrow(RoomStateInvalidException.auctionRoundMissing());
        given(settleAuction.settleIfDue("DUE02")).willReturn(due02.room());
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                scheduleReader,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames(due02.game())
            );

        scheduler.catchUpAndReschedule();

        verify(settleAuction).settleIfDue("BROKEN01");
        verify(settleAuction).settleIfDue("DUE02");
        assertThat(taskScheduler.activeScheduledInstants())
            .containsExactly(
                Instant.parse("2026-04-09T00:00:25Z"),
                Instant.parse("2026-04-09T00:00:15Z")
            );
    }

    @Test
    void 재시작_정산은_저장소_정렬보다_마감_시각을_우선한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        JpaAuctionScheduleReader scheduleReader =
            new JpaAuctionScheduleReader(new RecordingScheduleRepository(
                List.of(
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z")),
                    schedule("DUE01", Instant.parse("2026-04-09T00:00:05Z")),
                    schedule("LEGACY01", Instant.parse("2026-04-09T00:00:01Z"))
                )
            ));
        StartedAuctionContext legacy01 = auctionRoomWithDeadline("LEGACY01", Instant.parse("2026-04-09T00:00:20Z"));
        given(settleAuction.settleIfDue("LEGACY01")).willReturn(legacy01.room());
        given(settleAuction.settleIfDue("DUE01")).willReturn(completedAuctionRoom("DUE01"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                scheduleReader,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames(legacy01.game())
            );

        scheduler.catchUpAndReschedule();

        InOrder inOrder = inOrder(settleAuction);
        inOrder.verify(settleAuction).settleIfDue("LEGACY01");
        inOrder.verify(settleAuction).settleIfDue("DUE01");
        assertThat(taskScheduler.activeScheduledInstants())
            .containsExactly(
                Instant.parse("2026-04-09T00:00:20Z"),
                Instant.parse("2026-04-09T00:00:15Z")
            );
    }

    @Test
    void 재시작_예약은_첫_페이지를_넘는_미래_마감도_모두_처리한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        JpaAuctionScheduleReader scheduleReader = new JpaAuctionScheduleReader(new RecordingScheduleRepository(manyFutureSchedules(205)));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                scheduleReader,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames()
            );

        scheduler.catchUpAndReschedule();

        assertThat(taskScheduler.activeScheduledInstants()).hasSize(205);
        assertThat(taskScheduler.activeScheduledInstants()).contains(
            Instant.parse("2026-04-09T00:01:00Z"),
            Instant.parse("2026-04-09T00:04:24Z")
        );
    }

    private static StartedAuctionContext auctionRoomWithDeadline(String code, Instant deadline) {
        Room room =
            Room.createFromTemplate(
                code,
                new TeamLeaderId("host-" + code),
                "호스트",
                "host-token-" + code,
                new RoomTemplateSpec(
                    "LEAGUE_OF_LEGENDS",
                    RoomMode.AUCTION,
                    2,
                    2,
                    300,
                    15,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                Instant.parse("2026-04-09T00:00:00Z")
            );
        room.join(new TeamLeaderId("guest-" + code), "게스트", "guest-token-" + code);
        GameId gameId = new GameId(java.util.UUID.randomUUID());
        StartedGameSnapshot snapshot = room.start(
            new TeamLeaderId("host-" + code),
            gameId,
            Instant.parse("2026-04-09T00:00:00Z")
        );
        AuctionGame game = (AuctionGame) new GameFactory().create(snapshot);
        setCurrentAuctionRoundEndsAt(game, deadline);
        return new StartedAuctionContext(room, game);
    }

    private static void setCurrentAuctionRoundEndsAt(AuctionGame game, Instant deadline) {
        try {
            var field = AuctionGame.class.getDeclaredField("currentRoundEndsAt");
            field.setAccessible(true);
            field.set(game, deadline);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Room completedAuctionRoom(String code) {
        return auctionRoomWithDeadline(code, Instant.parse("2026-04-09T00:00:05Z")).room();
    }

    private static ScheduledAuctionGame schedule(String code, Instant deadline) {
        return new ScheduledAuctionGame(code, deadline);
    }

    private static List<ScheduledAuctionGame> manyFutureSchedules(int count) {
        List<ScheduledAuctionGame> candidates = new ArrayList<>();
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

    private record StartedAuctionContext(Room room, AuctionGame game) {
    }

    private record ScheduledAuctionGame(String code, Instant deadline) {
    }

    private static final class InMemoryGames implements Games {
        private final java.util.Map<GameId, Game> games;

        private InMemoryGames(Game... games) {
            this.games = new java.util.LinkedHashMap<>();
            for (Game game : games) {
                this.games.put(game.getId(), game);
            }
        }

        @Override
        public Game save(Game game) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Optional<Game> findById(GameId id) {
            return java.util.Optional.ofNullable(games.get(id));
        }
    }

    private static final class RecordingScheduleRepository implements AuctionScheduleJpaRepository {
        private final List<ScheduledAuctionGame> games;

        private RecordingScheduleRepository(ScheduledAuctionGame... games) {
            this.games = List.of(games);
        }

        private RecordingScheduleRepository(List<ScheduledAuctionGame> games) {
            this.games = List.copyOf(games);
        }

        @Override
        public List<AuctionGame> findByCurrentRoundEndsAtNotNullOrderByRoomCodeAsc(Pageable pageable) {
            int fromIndex = (int) pageable.getOffset();
            if (fromIndex >= games.size()) {
                return List.of();
            }
            int toIndex = Math.min(fromIndex + pageable.getPageSize(), games.size());
            return games.subList(fromIndex, toIndex).stream()
                .map(candidate -> auctionRoomWithDeadline(candidate.code(), candidate.deadline()).game())
                .toList();
        }
    }
}
