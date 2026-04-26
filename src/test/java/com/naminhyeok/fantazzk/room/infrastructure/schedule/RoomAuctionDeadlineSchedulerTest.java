package com.naminhyeok.fantazzk.room.infrastructure.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.AuctionSettlementRunner;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.AuctionParticipant;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomStateInvalidException;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.query.AuctionScheduleCandidate;
import com.naminhyeok.fantazzk.room.query.AuctionScheduleReader;
import com.naminhyeok.fantazzk.room.repository.Games;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomAuctionDeadlineSchedulerTest {
    private static final Instant NOW = Instant.parse("2026-04-09T00:00:10Z");

    @Test
    void 경매_마감_예약은_정산_후_다음_라운드_마감으로_이어진다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RecordingSettleAuction settleAuction = new RecordingSettleAuction();
        StartedAuctionContext room = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        StartedAuctionContext nextRoundRoom = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:30Z"));
        settleAuction.returnRoom("ROOM01", nextRoundRoom.room());
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                new RecordingScheduleReader(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames(room.game(), nextRoundRoom.game())
            );

        scheduler.schedule(room.room());

        assertThat(taskScheduler.scheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));

        taskScheduler.runLatest();

        assertThat(settleAuction.settledCodes()).containsExactly("ROOM01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
    }

    @Test
    void 같은_방의_마감은_하나만_활성화된다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RecordingSettleAuction settleAuction = new RecordingSettleAuction();
        StartedAuctionContext first = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        StartedAuctionContext second = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:20Z"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                new RecordingScheduleReader(),
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
        RecordingSettleAuction settleAuction = new RecordingSettleAuction();
        AuctionScheduleReader scheduleReader =
            new RecordingScheduleReader(
                schedule("DUE01", Instant.parse("2026-04-09T00:00:05Z")),
                schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
            );
        settleAuction.returnRoom("DUE01", completedAuctionRoom("DUE01"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                scheduleReader,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames()
            );

        scheduler.catchUpAndReschedule();

        assertThat(settleAuction.settledCodes()).containsExactly("DUE01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
    }

    @Test
    void 재시작_정산_중_손상된_방이_있어도_다음_방은_계속_처리한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RecordingSettleAuction settleAuction = new RecordingSettleAuction();
        AuctionScheduleReader scheduleReader =
            new RecordingScheduleReader(
                List.of(
                    schedule("BROKEN01", Instant.parse("2026-04-09T00:00:05Z")),
                    schedule("DUE02", Instant.parse("2026-04-09T00:00:06Z")),
                    schedule("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
                )
            );
        StartedAuctionContext due02 = auctionRoomWithDeadline("DUE02", Instant.parse("2026-04-09T00:00:25Z"));
        settleAuction.throwFailure("BROKEN01", RoomStateInvalidException.auctionRoundMissing());
        settleAuction.returnRoom("DUE02", due02.room());
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                scheduleReader,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new InMemoryGames(due02.game())
            );

        scheduler.catchUpAndReschedule();

        assertThat(settleAuction.settledCodes()).containsExactly("BROKEN01", "DUE02");
        assertThat(taskScheduler.activeScheduledInstants())
            .containsExactly(
                Instant.parse("2026-04-09T00:00:25Z"),
                Instant.parse("2026-04-09T00:00:15Z")
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
        GameId gameId = new GameId(UUID.randomUUID());
        room.start(
            new TeamLeaderId("host-" + code),
            gameId,
            Instant.parse("2026-04-09T00:00:00Z")
        );
        AuctionGame game =
            new AuctionGame(
                gameId,
                room.getId(),
                room.getCode(),
                room.getGameType(),
                room.getStartedAt(),
                GameRules.auction(
                    room.getTeamCount(),
                    room.getTeamSize(),
                    room.getBudget(),
                    room.getPickBanTime(),
                    room.getMinBidUnit()
                ),
                List.of(
                    new AuctionParticipant(new TeamLeaderId("host-" + code), "호스트", 300),
                    new AuctionParticipant(new TeamLeaderId("guest-" + code), "게스트", 300)
                ),
                room.getPlayers().stream()
                    .map(player -> new GamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                    .toList(),
                1,
                deadline
            );
        return new StartedAuctionContext(room, game);
    }

    private static Room completedAuctionRoom(String code) {
        return auctionRoomWithDeadline(code, Instant.parse("2026-04-09T00:00:05Z")).room();
    }

    private static AuctionScheduleCandidate schedule(String code, Instant deadline) {
        return new AuctionScheduleCandidate(code, deadline);
    }

    private record StartedAuctionContext(Room room, AuctionGame game) {
    }

    private static final class RecordingSettleAuction implements AuctionSettlementRunner {
        private final Map<String, Object> outcomes = new LinkedHashMap<>();
        private final List<String> settledCodes = new ArrayList<>();

        private void returnRoom(String code, Room room) {
            outcomes.put(code, room);
        }

        private void throwFailure(String code, RuntimeException failure) {
            outcomes.put(code, failure);
        }

        @Override
        public Room settleIfDue(String code) {
            settledCodes.add(code);
            Object outcome = outcomes.get(code);
            if (outcome instanceof RuntimeException failure) {
                throw failure;
            }
            if (outcome instanceof Room room) {
                return room;
            }
            throw new AssertionError("No settlement outcome registered for " + code);
        }

        private List<String> settledCodes() {
            return settledCodes;
        }
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

    private static final class RecordingScheduleReader implements AuctionScheduleReader {
        private final List<AuctionScheduleCandidate> candidates;

        private RecordingScheduleReader(AuctionScheduleCandidate... candidates) {
            this.candidates = List.of(candidates);
        }

        private RecordingScheduleReader(List<AuctionScheduleCandidate> candidates) {
            this.candidates = List.copyOf(candidates);
        }

        @Override
        public List<AuctionScheduleCandidate> findInProgressAuctionSchedules() {
            return candidates;
        }
    }
}
