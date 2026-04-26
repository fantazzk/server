package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.application.CreateRoom;
import com.naminhyeok.fantazzk.room.application.JoinRoom;
import com.naminhyeok.fantazzk.room.application.PickDraft;
import com.naminhyeok.fantazzk.room.application.PlaceBid;
import com.naminhyeok.fantazzk.room.application.RoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.application.SettleAuctionAttempt;
import com.naminhyeok.fantazzk.room.application.StartRoom;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameStatus;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.GameUpdatedEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEventFactory;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomUpdatedEvent;
import com.naminhyeok.fantazzk.room.query.TeamLeaderResponse;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.template.support.TemplateFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-realtime-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@Import(RoomRealtimeIntegrationTest.TestConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomRealtimeIntegrationTest {
    private static final Instant PUBLISHED_AT = Instant.parse("2026-04-13T10:15:30Z");

    private final TemplateFixture templateFixture;
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;
    private final PlaceBid placeBid;
    private final PickDraft pickDraft;
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Rooms rooms;
    private final RecordingRoomRealtimeEventPublisher recordingRoomRealtimeEventPublisher;
    private final PlatformTransactionManager transactionManager;
    private final MutableClock clock;

    @BeforeEach
    void clearPublishedEvents() {
        recordingRoomRealtimeEventPublisher.clear();
        clock.reset();
    }

    @Test
    void 방_참가가_커밋되면_로비_스냅샷을_하나만_발행한다() {
        UUID template =
            templateFixture.createAuctionTemplateId(
                "실시간 방",
                2,
                2,
                300,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        Room created = createRoom.create(template, "호스트").room();
        joinRoom.join(created.getCode(), "게스트");

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).singleElement()
            .isInstanceOfSatisfying(RoomUpdatedEvent.class, event -> {
                assertThat(event.roomCode()).isEqualTo(created.getCode());
                assertThat(event.snapshotVersion()).isEqualTo(reloaded.getVersion());
                assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);
                assertThat(event.room().status()).isEqualTo(RoomStatus.WAITING.name());
                assertThat(event.room().leaders()).extracting(TeamLeaderResponse::nickname)
                    .containsExactly("호스트", "게스트");
            });
    }

    @Test
    void 바깥_트랜잭션이_롤백되면_로비_스냅샷을_발행하지_않는다() {
        UUID template =
            templateFixture.createAuctionTemplateId(
                "실시간 롤백",
                2,
                2,
                300,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        Room created = createRoom.create(template, "호스트").room();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            joinRoom.join(created.getCode(), "게스트");
            throw new IllegalStateException("outer transaction rollback");
        })).isInstanceOf(IllegalStateException.class)
            .hasMessage("outer transaction rollback");

        List<String> leaderNicknames = transactionTemplate.execute(status -> rooms.findByCode(created.getCode())
            .orElseThrow()
            .getLeaders()
            .stream()
            .map(RoomTeamLeader::getNickname)
            .toList());

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).isEmpty();
        assertThat(leaderNicknames).containsExactly("호스트");
    }

    @Test
    void 경매_입찰과_정산은_게임_스냅샷으로_발행된다() {
        UUID template =
            templateFixture.createAuctionTemplateId(
                "실시간 경매",
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
        Game startedGame = startRoom.start(created.room().getCode(), created.leader().getActionToken());
        recordingRoomRealtimeEventPublisher.clear();

        placeBid.place(startedGame.getId().gameId(), guest.getActionToken(), 150);
        advancePastCurrentAuctionDeadline();
        settleAuctionAttempt.settleIfDue(created.room().getCode());

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).hasSize(2);
        GameUpdatedEvent firstEvent = (GameUpdatedEvent) recordingRoomRealtimeEventPublisher.publishedEvents().get(0);
        GameUpdatedEvent secondEvent = (GameUpdatedEvent) recordingRoomRealtimeEventPublisher.publishedEvents().get(1);
        assertThat(firstEvent.snapshotVersion()).isLessThan(secondEvent.snapshotVersion());
        assertThat(firstEvent.roomCode()).isEqualTo(created.room().getCode());
        assertThat(secondEvent.roomCode()).isEqualTo(created.room().getCode());
    }

    @Test
    void 드래프트_시작은_로비와_게임_스냅샷으로_발행된다() {
        UUID template =
            templateFixture.createDraftTemplateId(
                "실시간 드래프트 시작",
                2,
                2,
                com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        recordingRoomRealtimeEventPublisher.clear();

        selectDraftPositionsAndStart(created.room().getCode(), created.leader().getActionToken(), guest.getActionToken());

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).hasSize(2);
        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents().get(0))
            .isInstanceOfSatisfying(RoomUpdatedEvent.class, event -> {
                assertThat(event.room().status()).isEqualTo(RoomStatus.STARTED.name());
            });
        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents().get(1))
            .isInstanceOfSatisfying(GameUpdatedEvent.class, event -> {
                assertThat(event.game().status()).isEqualTo(GameStatus.IN_PROGRESS.name());
                assertThat(event.game().mode()).isEqualTo(RoomMode.DRAFT.name());
            });
    }

    @Test
    void 드래프트_픽은_게임_스냅샷으로_발행된다() {
        UUID template =
            templateFixture.createDraftTemplateId(
                "실시간 드래프트 픽",
                2,
                2,
                com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        selectDraftPositionsAndStart(created.room().getCode(), created.leader().getActionToken(), guest.getActionToken());
        recordingRoomRealtimeEventPublisher.clear();

        Room startedRoom = rooms.findByCode(created.room().getCode()).orElseThrow();
        pickDraft.pick(startedRoom.getStartedGameId().gameId(), created.leader().getActionToken(), "선수1");

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).singleElement()
            .isInstanceOf(GameUpdatedEvent.class);
    }

    @Test
    void 시작_스냅샷_이후_드래프트_진행_스냅샷_버전은_단조증가한다() {
        UUID template =
            templateFixture.createDraftTemplateId(
                "실시간 드래프트 버전",
                2,
                2,
                com.naminhyeok.fantazzk.template.TemplateCatalog.DraftOrderStrategy.SNAKE,
                List.of(
                    new TemplateFixture.PlayerSpec("선수1", "TOP"),
                    new TemplateFixture.PlayerSpec("선수2", "JUNGLE")
                )
            );

        RoomSessionResult created = createRoom.create(template, "호스트");
        RoomTeamLeader guest = joinRoom.join(created.room().getCode(), "게스트").leader();
        recordingRoomRealtimeEventPublisher.clear();

        selectDraftPositionsAndStart(created.room().getCode(), created.leader().getActionToken(), guest.getActionToken());
        long startSnapshotVersion = recordingRoomRealtimeEventPublisher.publishedEvents().getLast().snapshotVersion();

        recordingRoomRealtimeEventPublisher.clear();
        Room startedRoom = rooms.findByCode(created.room().getCode()).orElseThrow();
        pickDraft.pick(startedRoom.getStartedGameId().gameId(), created.leader().getActionToken(), "선수1");
        long firstPickSnapshotVersion = recordingRoomRealtimeEventPublisher.publishedEvents().getLast().snapshotVersion();

        assertThat(firstPickSnapshotVersion).isGreaterThan(startSnapshotVersion);
    }

    private void selectDraftPositionsAndStart(String code, String hostToken, String guestToken) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            Room room = rooms.findByCode(code).orElseThrow();
            room.selectDraftPosition(room.getLeaders().getFirst().getId(), 1);
            room.selectDraftPosition(room.getLeaders().getLast().getId(), 2);
        });
        startRoom.start(code, hostToken);
    }

    private void advancePastCurrentAuctionDeadline() {
        clock.advanceTo(clock.instant().plusSeconds(46));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        MutableClock roomSnapshotTestClock() {
            return new MutableClock(PUBLISHED_AT, ZoneOffset.UTC);
        }

        @Bean
        @Primary
        RecordingRoomRealtimeEventPublisher recordingRoomRealtimeEventPublisher(Clock clock) {
            return new RecordingRoomRealtimeEventPublisher(clock);
        }
    }

    static final class MutableClock extends Clock {
        private final Instant initialInstant;
        private final ZoneId zone;
        private Instant instant;

        private MutableClock(Instant initialInstant, ZoneId zone) {
            this.initialInstant = initialInstant;
            this.zone = zone;
            this.instant = initialInstant;
        }

        void reset() {
            instant = initialInstant;
        }

        void advanceTo(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    static final class RecordingRoomRealtimeEventPublisher implements RoomRealtimeEventPublisher {
        private final Clock clock;
        private final List<RoomRealtimeEvent> events = new ArrayList<>();

        RecordingRoomRealtimeEventPublisher(Clock clock) {
            this.clock = clock;
        }

        @Override
        public void publishRoomUpdatedAfterCommit(Room room) {
            publish(RoomRealtimeEventFactory.roomUpdated(room, Instant.now(clock)));
        }

        @Override
        public void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
            publish(RoomRealtimeEventFactory.gameUpdated(snapshot, Instant.now(clock)));
        }

        private void publish(RoomRealtimeEvent event) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                events.add(event);
                return;
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    events.add(event);
                }
            });
        }

        List<RoomRealtimeEvent> publishedEvents() {
            return List.copyOf(events);
        }

        void clear() {
            events.clear();
        }
    }
}
