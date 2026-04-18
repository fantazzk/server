package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.NoopRoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomSnapshotPublisherConfiguration;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.SupabaseRoomRealtimePublisher;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

class SupabaseRoomRealtimePublisherTest {
    private static final Instant PUBLISHED_AT = Instant.parse("2024-01-01T10:00:30Z");
    private static final String BASE_URL = "http://127.0.0.1:54321";
    private static final String SERVICE_ROLE_KEY = "service-role-key";

    @Test
    void publishAfterCommit는_트랜잭션이_없으면_즉시_broadcast_rest를_호출한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Room room = waitingAuctionRoom();
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andExpect(header("apikey", SERVICE_ROLE_KEY))
            .andExpect(header(AUTHORIZATION, "Bearer " + SERVICE_ROLE_KEY))
            .andExpect(jsonPath("$.messages[0].topic").value("room:" + room.getCode()))
            .andExpect(jsonPath("$.messages[0].event").value("snapshot.updated"))
            .andExpect(jsonPath("$.messages[0].payload.eventType").value("ROOM_SNAPSHOT_UPDATED"))
            .andExpect(jsonPath("$.messages[0].payload.snapshotVersion").value(room.getVersion()))
            .andExpect(jsonPath("$.messages[0].payload.publishedAt").value(PUBLISHED_AT.toString()))
            .andExpect(jsonPath("$.messages[0].payload.room.code").value(room.getCode()))
            .andRespond(withSuccess());

        publisher.publishAfterCommit(roomSnapshot(room));

        server.verify();
    }

    @Test
    void publishAfterCommit는_트랜잭션_동기화가_활성화되면_afterCommit에_보낸다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Room room = waitingAuctionRoom();
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andRespond(withSuccess());

        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publishAfterCommit(roomSnapshot(room));

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertThat(synchronizations).hasSize(1);

            TransactionSynchronizationManager.clearSynchronization();
            synchronizations.forEach(TransactionSynchronization::afterCommit);
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }

        server.verify();
    }

    @Test
    void publishAfterCommit는_스냅샷은_등록시점을_유지하고_publishedAt은_실제_전송시각을_쓴다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Room room = waitingAuctionRoom();
        long snapshotVersion = room.getVersion();
        MutableClock clock = new MutableClock(PUBLISHED_AT);
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, clock, BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andExpect(jsonPath("$.messages[0].payload.roomCode").value(room.getCode()))
            .andExpect(jsonPath("$.messages[0].payload.snapshotVersion").value(snapshotVersion))
            .andExpect(jsonPath("$.messages[0].payload.room.teamLeaders.length()").value(1))
            .andExpect(jsonPath("$.messages[0].payload.room.teamLeaders[0].nickname").value("호스트"))
            .andExpect(jsonPath("$.messages[0].payload.publishedAt").value(PUBLISHED_AT.plusSeconds(30).toString()))
            .andRespond(withSuccess());

        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publishAfterCommit(roomSnapshot(room));
            room.join(new TeamLeaderId("leader-guest"), "게스트", "guest-token");
            clock.advanceSeconds(30);

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertThat(synchronizations).hasSize(1);

            TransactionSynchronizationManager.clearSynchronization();
            synchronizations.forEach(TransactionSynchronization::afterCommit);
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }

        server.verify();
    }

    @Test
    void publishAfterCommit는_started_snapshot을_game_payload로_broadcast한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        StartedRoomSnapshot snapshot = startedAuctionSnapshot();
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andExpect(jsonPath("$.messages[0].payload.eventType").value("GAME_SNAPSHOT_UPDATED"))
            .andExpect(jsonPath("$.messages[0].payload.roomCode").value(snapshot.roomCode()))
            .andExpect(jsonPath("$.messages[0].payload.room").doesNotExist())
            .andExpect(jsonPath("$.messages[0].payload.game.id").value(snapshot.game().id()))
            .andExpect(jsonPath("$.messages[0].payload.game.roomCode").value(snapshot.roomCode()))
            .andExpect(jsonPath("$.messages[0].payload.game.progress.currentRound").value(1))
            .andRespond(withSuccess());

        publisher.publishAfterCommit(snapshot);

        server.verify();
    }

    @Test
    void publishAfterCommit는_전송_실패를_호출자에게_전파하지_않는다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andRespond(withServerError());

        assertThatCode(() -> publisher.publishAfterCommit(roomSnapshot(waitingAuctionRoom()))).doesNotThrowAnyException();

        server.verify();
    }

    @Test
    void realtime_enabled가_false면_noop_publisher가_선택된다() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                .getPropertySources()
                .addFirst(new MapPropertySource("test", java.util.Map.of("fantazzk.supabase.realtime.enabled", "false")));
            context.register(빈선택지원설정.class, RoomSnapshotPublisherConfiguration.class);
            context.refresh();

            assertThat(context.getBean(RoomSnapshotPublisher.class)).isInstanceOf(NoopRoomSnapshotPublisher.class);
        }
    }

    @Test
    void realtime_enabled가_true면_supabase_publisher가_선택된다() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                .getPropertySources()
                .addFirst(
                    new MapPropertySource(
                        "test",
                        java.util.Map.of(
                            "fantazzk.supabase.realtime.enabled",
                            "true",
                            "fantazzk.supabase.url",
                            BASE_URL,
                            "fantazzk.supabase.service-role-key",
                            SERVICE_ROLE_KEY
                        )
                    )
                );
            context.register(빈선택지원설정.class, RoomSnapshotPublisherConfiguration.class);
            context.refresh();

            assertThat(context.getBean(RoomSnapshotPublisher.class)).isInstanceOf(SupabaseRoomRealtimePublisher.class);
        }
    }

    @Test
    void 다른_roomSnapshotPublisher가_있으면_supabase_publisher를_추가로_등록하지_않는다() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                .getPropertySources()
                .addFirst(
                    new MapPropertySource(
                        "test",
                        java.util.Map.of(
                            "fantazzk.supabase.realtime.enabled",
                            "true",
                            "fantazzk.supabase.url",
                            BASE_URL,
                            "fantazzk.supabase.service-role-key",
                            SERVICE_ROLE_KEY
                        )
                    )
                );
            context.register(빈선택지원설정.class, 대체퍼블리셔설정.class, RoomSnapshotPublisherConfiguration.class);
            context.refresh();

            assertThat(context.getBeansOfType(RoomSnapshotPublisher.class)).hasSize(1);
            assertThat(context.getBean(RoomSnapshotPublisher.class)).isSameAs(context.getBean("otherPublisher"));
        }
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "ROOM01",
            new TeamLeaderId("leader-host"),
            "호스트",
            "host-token",
            new RoomTemplateSpec(
                RoomMode.AUCTION,
                2,
                2,
                300,
                30,
                10,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            Instant.parse("2024-01-01T10:00:00Z")
        );
    }

    private static StartedRoomSnapshot startedAuctionSnapshot() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId("leader-guest"), "게스트", "guest-token");
        StartedGameSnapshot snapshot = room.start(
            new TeamLeaderId("leader-host"),
            new GameId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000101")),
            Instant.parse("2024-01-01T10:00:00Z")
        );
        Game game = new GameFactory().create(snapshot);
        return new StartedRoomSnapshot(room.getCode(), room.getVersion() + game.getVersion(), GameView.from(game));
    }

    private static RoomSnapshot roomSnapshot(Room room) {
        return new RoomSnapshot(room.getCode(), room.getVersion(), RoomView.from(room));
    }

    @Configuration(proxyBeanMethods = false)
    static class 빈선택지원설정 {
        @Bean
        Clock clock() {
            return Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC);
        }

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class 대체퍼블리셔설정 {
        @Bean
        RoomSnapshotPublisher otherPublisher() {
            return new NoopRoomSnapshotPublisher();
        }
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        private MutableClock(Instant currentInstant) {
            this.currentInstant = currentInstant;
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
            return currentInstant;
        }

        private void advanceSeconds(long seconds) {
            currentInstant = currentInstant.plusSeconds(seconds);
        }
    }
}
