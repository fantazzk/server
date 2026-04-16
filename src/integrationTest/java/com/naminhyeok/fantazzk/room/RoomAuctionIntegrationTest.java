package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateFixture;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-auction-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@Import(RoomAuctionIntegrationTest.FixedClockConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomAuctionIntegrationTest {
    private static final Instant INITIAL_TIME = Instant.parse("2000-01-01T00:00:00Z");

    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final PlaceBid placeBid;
    private final SettleAuction settleAuction;
    private final Rooms rooms;
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;
    private final RecordingTaskScheduler recordingTaskScheduler;
    private final PlatformTransactionManager transactionManager;
    private final MutableClock roomAuctionTestClock;

    @BeforeEach
    void clearScheduledTasks() {
        recordingTaskScheduler.clear();
        roomAuctionTestClock.setInstant(INITIAL_TIME);
    }

    @Test
    @Transactional
    void 입찰과_정산을_처리하면_선수_배정과_예산_차감이_반영된다() {
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
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        startRoom.start(created.room().getCode(), created.leader().getActionToken());

        Room roomAfterBid = placeBid.place(created.room().getCode(), guest.getActionToken(), 150);
        roomAuctionTestClock.setInstant(INITIAL_TIME.plusSeconds(45));
        AuctionSettlement settlement = settleAuction.settle(created.room().getCode());

        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();

        assertThat(roomAfterBid.getBids()).singleElement()
            .extracting(RoomBid::teamLeaderId, RoomBid::amount)
            .containsExactly(guest.getId(), 150);
        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerId()).isEqualTo(new RoomPlayerId(0));
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(reloaded.getPlayers().getFirst().getPosition()).isEqualTo("TOP");
        assertThat(reloaded.getMembers()).singleElement()
            .extracting(RoomTeamMember::teamLeaderId, RoomTeamMember::playerId)
            .containsExactly(guest.getId(), new RoomPlayerId(0));
        assertThat(reloaded.getLeaders().stream().filter(it -> it.getId().equals(guest.getId())).findFirst().orElseThrow().getRemainingBudget())
            .isEqualTo(150);
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(2);
    }

    @Test
    void 같은_라운드의_입찰_순번은_재조회_후에도_누적된다() {
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
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        startRoom.start(created.room().getCode(), created.leader().getActionToken());

        Room firstBid = placeBid.place(created.room().getCode(), created.leader().getActionToken(), 100);
        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();
        Room secondBid = placeBid.place(reloaded.getCode(), guest.getActionToken(), 150);

        assertThat(firstBid.getBids().getLast().sequence()).isEqualTo(new BidSequence(1));
        assertThat(secondBid.getBids().getLast().sequence()).isEqualTo(new BidSequence(2));
        assertThat(secondBid.getBids().getLast().round()).isEqualTo(1);
    }

    @Test
    void 같은_포지션_제한에_걸리는_선수에게는_재조회_후에도_입찰할_수_없다() {
        var template =
            templateFixture.createAuctionTemplateId(
                "포지션제한경매전",
                2,
                3,
                300,
                List.of(
                    new TemplateFixture.PlayerSpec("탑선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("탑선수2", "TOP"),
                    new TemplateFixture.PlayerSpec("정글선수1", "JUNGLE"),
                    new TemplateFixture.PlayerSpec("미드선수1", "MID")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        startRoom.start(created.room().getCode(), created.leader().getActionToken());

        placeBid.place(created.room().getCode(), guest.getActionToken(), 150);
        roomAuctionTestClock.setInstant(INITIAL_TIME.plusSeconds(45));
        settleAuction.settle(created.room().getCode());

        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();

        assertThatThrownBy(() -> placeBid.place(reloaded.getCode(), guest.getActionToken(), 160))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError().getCode()).isEqualTo("ROOM_AUCTION_POSITION_LIMIT_EXCEEDED")
            );
    }

    @Test
    void catchUpAndReschedule는_due_room을_즉시_정산한다() {
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
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        startRoom.start(created.room().getCode(), created.leader().getActionToken());
        placeBid.place(created.room().getCode(), guest.getActionToken(), 150);
        roomAuctionTestClock.setInstant(INITIAL_TIME.plusSeconds(45));

        roomAuctionDeadlineScheduler.catchUpAndReschedule();

        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(2);
        assertThat(reloaded.getCurrentAuctionRoundEndsAt()).isEqualTo(INITIAL_TIME.plusSeconds(90));
    }

    @Test
    void start_rollback되면_deadline_task는_등록되지_않는다() {
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

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            startRoom.start(created.room().getCode(), created.leader().getActionToken());
            status.setRollbackOnly();
        });

        assertThat(recordingTaskScheduler.scheduledInstants()).isEmpty();
        assertThat(rooms.findByCode(created.room().getCode()).orElseThrow().getStatus()).isEqualTo(RoomStatus.WAITING);
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        MutableClock roomAuctionTestClock() {
            return new MutableClock(INITIAL_TIME);
        }

        @Bean
        @Primary
        RecordingTaskScheduler roomAuctionIntegrationTaskScheduler() {
            return new RecordingTaskScheduler();
        }
    }

    static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    static final class RecordingTaskScheduler implements TaskScheduler {
        private final List<ScheduledTask> scheduledTasks = new ArrayList<>();

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            return new ScheduledTask(null);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            ScheduledTask scheduledTask = new ScheduledTask(startTime);
            scheduledTasks.add(scheduledTask);
            return scheduledTask;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(
            Runnable task,
            Instant startTime,
            Duration period
        ) {
            return new ScheduledTask(startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
            return new ScheduledTask(null);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(
            Runnable task,
            Instant startTime,
            Duration delay
        ) {
            return new ScheduledTask(startTime);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
            return new ScheduledTask(null);
        }

        List<Instant> scheduledInstants() {
            return scheduledTasks.stream()
                .filter(task -> !task.isCancelled())
                .filter(task -> task.scheduledAt() != null)
                .map(ScheduledTask::scheduledAt)
                .sorted(Comparator.naturalOrder())
                .toList();
        }

        void clear() {
            scheduledTasks.clear();
        }
    }

    static final class ScheduledTask implements ScheduledFuture<Object> {
        private final Instant scheduledAt;
        private boolean cancelled;

        private ScheduledTask(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
        }

        Instant scheduledAt() {
            return scheduledAt;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return cancelled;
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
