package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.application.CreateRoom;
import com.naminhyeok.fantazzk.room.application.JoinRoom;
import com.naminhyeok.fantazzk.room.application.PickDraft;
import com.naminhyeok.fantazzk.room.application.PlaceBid;
import com.naminhyeok.fantazzk.room.application.RoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.application.SettleAuction;
import com.naminhyeok.fantazzk.room.application.StartRoom;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
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
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.room.query.GameMemberResponse;
import com.naminhyeok.fantazzk.room.query.GameParticipantResponse;
import com.naminhyeok.fantazzk.room.query.TeamLeaderResponse;
import com.naminhyeok.fantazzk.template.TemplateFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
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
    private final SettleAuction settleAuction;
    private final Rooms rooms;
    private final Games games;
    private final RecordingRoomRealtimeEventPublisher recordingRoomRealtimeEventPublisher;
    private final PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearPublishedEvents() {
        recordingRoomRealtimeEventPublisher.clear();
    }

    @Test
    void join이_커밋되면_room_updated를_정확히_하나_publish한다() {
        var template =
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
    void 바깥_트랜잭션이_롤백되면_room_snapshot을_publish하지_않는다() {
        var template =
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
    void 경매_입찰과_정산_publish는_game_updated를_사용한다() {
        var template =
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
        startRoom.start(created.room().getCode(), created.leader().getActionToken());
        recordingRoomRealtimeEventPublisher.clear();

        placeBid.place(created.room().getCode(), guest.getActionToken(), 150);
        expireAuctionRound(created.room().getCode(), Instant.parse("1999-12-31T23:59:55Z"));
        settleAuction.settle(created.room().getCode());
        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();
        AuctionGame game = (AuctionGame) games.findById(reloaded.getStartedGameId()).orElseThrow();

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).hasSize(2);
        GameUpdatedEvent firstEvent = (GameUpdatedEvent) recordingRoomRealtimeEventPublisher.publishedEvents().get(0);
        GameUpdatedEvent secondEvent = (GameUpdatedEvent) recordingRoomRealtimeEventPublisher.publishedEvents().get(1);
        assertThat(firstEvent.snapshotVersion()).isLessThan(secondEvent.snapshotVersion());
        assertThat(firstEvent.game().roster()).isEmpty();
        assertThat(firstEvent.game().auctionProgress().highestBidAmount()).isEqualTo(150);
        assertThat(secondEvent)
            .isInstanceOfSatisfying(GameUpdatedEvent.class, event -> {
                assertThat(event.snapshotVersion()).isEqualTo(reloaded.getVersion() + game.getVersion());
                assertThat(event.game().roster())
                    .extracting(GameMemberResponse::teamLeaderId, GameMemberResponse::playerName)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple(guest.getId().value(), "선수1"));
                assertThat(event.game().participants())
                    .extracting(GameParticipantResponse::teamLeaderId, GameParticipantResponse::remainingBudget)
                    .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(created.leader().getId().value(), 300),
                        org.assertj.core.groups.Tuple.tuple(guest.getId().value(), 150)
                    );
                assertThat(event.game().auctionProgress().currentRound()).isEqualTo(2);
            });
    }

    @Test
    void started_draft_room의_auction_progress는_기존처럼_noop_응답을_유지한다() {
        var template =
            templateFixture.createDraftTemplateId(
                "실시간 드래프트",
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

        Room current = settleAuction.settleIfDue(created.room().getCode());

        assertThat(current.getMode()).isEqualTo(RoomMode.DRAFT);
        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).isEmpty();
    }

    @Test
    void 드래프트_start_publish는_room_updated와_game_updated를_사용한다() {
        var template =
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
        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();
        DraftGame game = (DraftGame) games.findById(reloaded.getStartedGameId()).orElseThrow();

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).hasSize(2);
        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents().get(0))
            .isInstanceOfSatisfying(RoomUpdatedEvent.class, event -> {
                assertThat(event.snapshotVersion()).isEqualTo(reloaded.getVersion());
                assertThat(event.room().startedGameId()).isEqualTo(game.getId().gameId().toString());
                assertThat(event.room().status()).isEqualTo(RoomStatus.STARTED.name());
            });
        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents().get(1))
            .isInstanceOfSatisfying(GameUpdatedEvent.class, event -> {
                assertThat(event.snapshotVersion()).isEqualTo(reloaded.getVersion() + game.getVersion());
                assertThat(event.game().gameId()).isEqualTo(game.getId().gameId().toString());
                assertThat(event.game().status()).isEqualTo(GameStatus.IN_PROGRESS.name());
                assertThat(event.game().mode()).isEqualTo(RoomMode.DRAFT.name());
            });
    }

    @Test
    void 드래프트_픽_publish는_game_updated를_반영한다() {
        var template =
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

        pickDraft.pick(created.room().getCode(), created.leader().getActionToken(), "선수1");
        Room reloaded = rooms.findByCode(created.room().getCode()).orElseThrow();
        DraftGame game = (DraftGame) games.findById(reloaded.getStartedGameId()).orElseThrow();

        assertThat(recordingRoomRealtimeEventPublisher.publishedEvents()).singleElement()
            .isInstanceOfSatisfying(GameUpdatedEvent.class, event -> {
                assertThat(event.snapshotVersion()).isEqualTo(reloaded.getVersion() + game.getVersion());
                assertThat(event.game().roster())
                    .extracting(GameMemberResponse::teamLeaderId, GameMemberResponse::playerName)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple(created.leader().getId().value(), "선수1"));
                assertThat(event.game().draftProgress()).satisfies(progress -> {
                    assertThat(progress.currentTurnIndex()).isEqualTo(1);
                    assertThat(progress.currentLeaderId()).isEqualTo(guest.getId().value());
                });
            });
    }

    @Test
    void 시작_스냅샷_이후_드래프트_live_update의_snapshot_version은_단조증가한다() {
        var template =
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
        pickDraft.pick(created.room().getCode(), created.leader().getActionToken(), "선수1");
        long firstPickSnapshotVersion = recordingRoomRealtimeEventPublisher.publishedEvents().getLast().snapshotVersion();

        assertThat(firstPickSnapshotVersion).isGreaterThan(startSnapshotVersion);
    }

    private void selectDraftPositionsAndStart(String code, String hostToken, String guestToken) {
        var tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            Room room = rooms.findByCode(code).orElseThrow();
            room.selectDraftPosition(room.getLeaders().getFirst().getId(), 1);
            room.selectDraftPosition(room.getLeaders().getLast().getId(), 2);
        });
        startRoom.start(code, hostToken);
    }

    private void expireAuctionRound(String code, Instant deadline) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            Room room = rooms.findByCode(code).orElseThrow();
            AuctionGame game = (AuctionGame) games.findById(room.getStartedGameId()).orElseThrow();
            setCurrentAuctionRoundEndsAt(game, deadline);
        });
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        Clock roomSnapshotTestClock() {
            return Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC);
        }

        @Bean
        @Primary
        RecordingRoomRealtimeEventPublisher recordingRoomRealtimeEventPublisher(Clock clock) {
            return new RecordingRoomRealtimeEventPublisher(clock);
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
