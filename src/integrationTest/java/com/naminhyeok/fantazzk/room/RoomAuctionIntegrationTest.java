package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    void clearScheduledTasks() {
        recordingTaskScheduler.clear();
    }

    @Test
    @Transactional
    void 입찰과_정산을_처리하면_선수_배정과_예산_차감이_반영된다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        RoomBid bid = placeBid.place(created.getCode(), guest.getActionToken(), 150);
        Room roomBeforeSettlement = rooms.findByCode(created.getCode()).orElseThrow();
        setCurrentAuctionRoundEndsAt(roomBeforeSettlement, Instant.parse("1999-12-31T23:59:55Z"));
        rooms.saveAndFlush(roomBeforeSettlement);
        AuctionSettlement settlement = settleAuction.settle(created.getCode());

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(bid.teamLeaderId()).isEqualTo(guest.getId());
        assertThat(bid.amount()).isEqualTo(150);
        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(reloaded.getBids()).singleElement()
            .extracting(RoomBid::teamLeaderId, RoomBid::amount)
            .containsExactly(guest.getId(), 150);
        assertThat(reloaded.getMembers()).singleElement()
            .extracting(RoomTeamMember::teamLeaderId, RoomTeamMember::playerName)
            .containsExactly(guest.getId(), "선수1");
        assertThat(reloaded.getLeaders().stream().filter(it -> it.getId().equals(guest.getId())).findFirst().orElseThrow().getRemainingBudget())
            .isEqualTo(150);
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(2);
    }

    @Test
    void 같은_라운드의_입찰_순번은_재조회_후에도_누적된다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        RoomBid firstBid = placeBid.place(created.getCode(), created.getLeaders().getFirst().getActionToken(), 100);
        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();
        RoomBid secondBid = placeBid.place(reloaded.getCode(), guest.getActionToken(), 150);

        assertThat(firstBid.sequence()).isEqualTo(new BidSequence(1));
        assertThat(secondBid.sequence()).isEqualTo(new BidSequence(2));
        assertThat(secondBid.round()).isEqualTo(1);
    }

    @Test
    void catchUpAndReschedule는_due_room을_즉시_정산한다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());
        placeBid.place(created.getCode(), guest.getActionToken(), 150);

        Room room = rooms.findByCode(created.getCode()).orElseThrow();
        setCurrentAuctionRoundEndsAt(room, Instant.parse("1999-12-31T23:59:55Z"));
        rooms.saveAndFlush(room);

        roomAuctionDeadlineScheduler.catchUpAndReschedule();

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();
        assertThat(reloaded.getCurrentAuctionRound()).isEqualTo(2);
        assertThat(reloaded.getCurrentAuctionRoundEndsAt()).isEqualTo(Instant.parse("2000-01-01T00:00:15Z"));
    }

    @Test
    void legacy_null_deadline_방에_입찰하면_deadline을_복구하고_commit후_재예약한다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.getCode(), "게스트");
        startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());

        Room room = rooms.findByCode(created.getCode()).orElseThrow();
        setCurrentAuctionRoundEndsAt(room, null);
        rooms.saveAndFlush(room);
        recordingTaskScheduler.clear();

        placeBid.place(created.getCode(), guest.getActionToken(), 150);

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();
        assertThat(reloaded.getCurrentAuctionRoundEndsAt()).isEqualTo(Instant.parse("2000-01-01T00:00:15Z"));
        assertThat(recordingTaskScheduler.scheduledInstants()).containsExactly(Instant.parse("2000-01-01T00:00:15Z"));
    }

    @Test
    void start_rollback되면_deadline_task는_등록되지_않는다() {
        var template =
            templateFixture.createAuctionTemplateId("경매전", 2, 2, 300, List.of("선수1", "선수2"));

        Room created = createRoom.create(template, "호스트");
        joinRoom.join(created.getCode(), "게스트");

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            startRoom.start(created.getCode(), created.getLeaders().getFirst().getActionToken());
            status.setRollbackOnly();
        });

        assertThat(recordingTaskScheduler.scheduledInstants()).isEmpty();
        assertThat(rooms.findByCode(created.getCode()).orElseThrow().getStatus()).isEqualTo(RoomStatus.WAITING);
    }

    private static void setCurrentAuctionRoundEndsAt(Room room, Instant deadline) {
        try {
            var field = Room.class.getDeclaredField("currentAuctionRoundEndsAt");
            field.setAccessible(true);
            field.set(room, deadline);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    @TestConfiguration
    static class FixedClockConfiguration {
        @Bean
        @Primary
        Clock roomAuctionTestClock() {
            return Clock.fixed(Instant.parse("2000-01-01T00:00:00Z"), ZoneOffset.UTC);
        }

        @Bean
        @Primary
        RecordingTaskScheduler roomAuctionIntegrationTaskScheduler() {
            return new RecordingTaskScheduler();
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
