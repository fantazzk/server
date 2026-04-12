package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private final Rooms rooms;
    private final RecordingRoomRealtimePublisher recordingRoomRealtimePublisher;
    private final PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearPublishedEvents() {
        recordingRoomRealtimePublisher.clear();
    }

    @Test
    void join이_커밋되면_latest_room_snapshot을_정확히_하나_publish한다() {
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

        Room created = createRoom.create(template, "호스트");
        joinRoom.join(created.getCode(), "게스트");

        Room reloaded = rooms.findByCode(created.getCode()).orElseThrow();

        assertThat(recordingRoomRealtimePublisher.publishedEvents()).singleElement()
            .satisfies(event -> {
                assertThat(event.roomCode()).isEqualTo(created.getCode());
                assertThat(event.snapshotVersion()).isEqualTo(reloaded.getVersion());
                assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);
                assertThat(event.room().status()).isEqualTo(RoomStatus.WAITING.name());
                assertThat(event.room().teamLeaders()).extracting(TeamLeaderResponse::nickname)
                    .containsExactly("호스트", "게스트");
                assertThat(event.room().members()).isEmpty();
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

        Room created = createRoom.create(template, "호스트");
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

        assertThat(recordingRoomRealtimePublisher.publishedEvents()).isEmpty();
        assertThat(leaderNicknames).containsExactly("호스트");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        Clock roomRealtimeTestClock() {
            return Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC);
        }

        @Bean
        @Primary
        RecordingRoomRealtimePublisher recordingRoomRealtimePublisher(Clock clock) {
            return new RecordingRoomRealtimePublisher(clock);
        }
    }

    static final class RecordingRoomRealtimePublisher implements RoomRealtimePublisher {
        private final Clock clock;
        private final List<RoomRealtimeSnapshotEvent> events = new ArrayList<>();

        RecordingRoomRealtimePublisher(Clock clock) {
            this.clock = clock;
        }

        @Override
        public void publishAfterCommit(Room room) {
            RoomRealtimeSnapshotEvent event = RoomRealtimeSnapshotEvent.from(room, Instant.now(clock));
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

        List<RoomRealtimeSnapshotEvent> publishedEvents() {
            return List.copyOf(events);
        }

        void clear() {
            events.clear();
        }
    }
}
