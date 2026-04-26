package com.naminhyeok.fantazzk.room.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.CreateRoom;
import com.naminhyeok.fantazzk.room.application.JoinRoom;
import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.application.SelectDraftPosition;
import com.naminhyeok.fantazzk.room.application.StartRoom;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.support.TemplateFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-service-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@Import(RoomServiceIntegrationTest.FixedClockConfiguration.class)
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomServiceIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-04-15T00:00:00Z");

    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final SelectDraftPosition selectDraftPosition;
    private final Rooms rooms;
    private final Games games;
    private final RecordingTaskScheduler recordingTaskScheduler;

    @BeforeEach
    void 예약_기록을_초기화한다() {
        recordingTaskScheduler.clear();
    }

    @Test
    void 경매전_시작은_게임을_생성해_같은_ID로_저장하고_deadline을_예약한다() {
        var template =
            templateFixture.createAuctionTemplateId(
                "경매전",
                2,
                2,
                300,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        joinRoom.join(created.room().getCode(), "게스트");

        startRoom.start(created.room().getCode(), created.leader().getActionToken());
        assertThat(recordingTaskScheduler.scheduledInstants()).isEmpty();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        Room reloadedRoom = rooms.findByCode(created.room().getCode()).orElseThrow();
        Game reloadedGame = games.findById(reloadedRoom.getStartedGameId()).orElseThrow();

        assertThat(reloadedRoom.getStartedGameId()).isNotNull();
        assertThat(reloadedRoom.getStartedAt()).isEqualTo(NOW);
        assertThat(reloadedGame.getId()).isEqualTo(reloadedRoom.getStartedGameId());
        assertThat(reloadedGame).isInstanceOf(AuctionGame.class);
        AuctionGame auctionGame = (AuctionGame) reloadedGame;
        assertThat(auctionGame.getCurrentRound()).isEqualTo(1);
        assertThat(auctionGame.getCurrentRoundEndsAt()).isEqualTo(NOW.plusSeconds(reloadedRoom.getPickBanTime()));
        assertThat(recordingTaskScheduler.scheduledInstants()).containsExactly(NOW.plusSeconds(reloadedRoom.getPickBanTime()));
    }

    @Test
    void 드래프트전_시작은_게임을_생성하지만_deadline을_예약하지_않는다() {
        var template =
            templateFixture.createDraftTemplateId(
                "드래프트전",
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        selectDraftPosition.select(created.room().getCode(), created.leader().getActionToken(), 1);
        selectDraftPosition.select(created.room().getCode(), guest.getActionToken(), 2);

        startRoom.start(created.room().getCode(), created.leader().getActionToken());
        assertThat(recordingTaskScheduler.scheduledInstants()).isEmpty();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        Room reloadedRoom = rooms.findByCode(created.room().getCode()).orElseThrow();
        Game reloadedGame = games.findById(reloadedRoom.getStartedGameId()).orElseThrow();

        assertThat(reloadedGame).isInstanceOf(DraftGame.class);
        assertThat(((DraftGame) reloadedGame).getCurrentTurnIndex()).isEqualTo(0);
        assertThat(recordingTaskScheduler.scheduledInstants()).isEmpty();
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock roomServiceTestClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }

        @Bean
        @Primary
        RecordingTaskScheduler roomServiceTestTaskScheduler() {
            return new RecordingTaskScheduler();
        }
    }

    static final class RecordingTaskScheduler implements TaskScheduler {
        private final java.util.ArrayList<Instant> scheduledInstants = new java.util.ArrayList<>();

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            return new NoopScheduledFuture(null);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            scheduledInstants.add(startTime);
            return new NoopScheduledFuture(startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, java.time.Duration period) {
            return new NoopScheduledFuture(startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, java.time.Duration period) {
            return new NoopScheduledFuture(null);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, java.time.Duration delay) {
            return new NoopScheduledFuture(startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, java.time.Duration delay) {
            return new NoopScheduledFuture(null);
        }

        List<Instant> scheduledInstants() {
            return List.copyOf(scheduledInstants);
        }

        void clear() {
            scheduledInstants.clear();
        }
    }

    static final class NoopScheduledFuture implements ScheduledFuture<Object> {
        private final Instant scheduledAt;

        private NoopScheduledFuture(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            if (scheduledAt == null) {
                return 0;
            }
            return unit.convert(Math.max(0, scheduledAt.toEpochMilli() - NOW.toEpochMilli()), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }
    }
}
