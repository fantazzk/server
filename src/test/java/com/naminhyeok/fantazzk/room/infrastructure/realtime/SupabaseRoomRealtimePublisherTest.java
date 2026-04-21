package com.naminhyeok.fantazzk.room.infrastructure.realtime;

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

import com.naminhyeok.fantazzk.room.application.RoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.NoopRoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEventPublisherConfiguration;
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
    void publishRoomUpdatedAfterCommit는_트랜잭션이_없으면_즉시_broadcast_rest를_호출한다() {
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
            .andExpect(jsonPath("$.messages[0].event").value("room.updated"))
            .andExpect(jsonPath("$.messages[0].payload.eventType").value("ROOM_UPDATED"))
            .andExpect(jsonPath("$.messages[0].payload.snapshotVersion").value(room.getVersion()))
            .andExpect(jsonPath("$.messages[0].payload.publishedAt").value(PUBLISHED_AT.toString()))
            .andExpect(jsonPath("$.messages[0].payload.room.leaders[0].nickname").value("호스트"))
            .andRespond(withSuccess());

        publisher.publishRoomUpdatedAfterCommit(room);

        server.verify();
    }

    @Test
    void publishRoomUpdatedAfterCommit는_트랜잭션_동기화가_활성화되면_afterCommit에_보낸다() {
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
            publisher.publishRoomUpdatedAfterCommit(room);

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
    void publishRoomUpdatedAfterCommit는_스냅샷은_등록시점을_유지하고_publishedAt은_실제_전송시각을_쓴다() {
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
            .andExpect(jsonPath("$.messages[0].payload.room.leaders.length()").value(1))
            .andExpect(jsonPath("$.messages[0].payload.room.leaders[0].nickname").value("호스트"))
            .andExpect(jsonPath("$.messages[0].payload.publishedAt").value(PUBLISHED_AT.plusSeconds(30).toString()))
            .andRespond(withSuccess());

        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publishRoomUpdatedAfterCommit(room);
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
    void publishGameUpdatedAfterCommit는_started_payload를_broadcast한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        StartedRoomSnapshot snapshot = startedAuctionSnapshot();
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andExpect(jsonPath("$.messages[0].event").value("game.updated"))
            .andExpect(jsonPath("$.messages[0].payload.eventType").value("GAME_UPDATED"))
            .andExpect(jsonPath("$.messages[0].payload.roomCode").value(snapshot.room().getCode()))
            .andExpect(jsonPath("$.messages[0].payload.game.gameId").value(snapshot.game().getId().gameId().toString()))
            .andExpect(jsonPath("$.messages[0].payload.game.mode").value("AUCTION"))
            .andRespond(withSuccess());

        publisher.publishGameUpdatedAfterCommit(snapshot);

        server.verify();
    }

    @Test
    void publishRoomUpdatedAfterCommit는_전송_실패를_호출자에게_전파하지_않는다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        SupabaseRoomRealtimePublisher publisher =
            new SupabaseRoomRealtimePublisher(builder, Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), BASE_URL, SERVICE_ROLE_KEY, "room");

        server.expect(requestTo(BASE_URL + "/realtime/v1/api/broadcast"))
            .andExpect(method(POST))
            .andRespond(withServerError());

        assertThatCode(() -> publisher.publishRoomUpdatedAfterCommit(waitingAuctionRoom())).doesNotThrowAnyException();

        server.verify();
    }

    @Test
    void realtime_enabled가_false면_noop_publisher가_선택된다() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                .getPropertySources()
                .addFirst(new MapPropertySource("test", java.util.Map.of("fantazzk.supabase.realtime.enabled", "false")));
            context.register(빈선택지원설정.class, RoomRealtimeEventPublisherConfiguration.class);
            context.refresh();

            assertThat(context.getBean(RoomRealtimeEventPublisher.class)).isInstanceOf(NoopRoomRealtimeEventPublisher.class);
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
            context.register(빈선택지원설정.class, RoomRealtimeEventPublisherConfiguration.class);
            context.refresh();

            assertThat(context.getBean(RoomRealtimeEventPublisher.class)).isInstanceOf(SupabaseRoomRealtimePublisher.class);
        }
    }

    @Test
    void 다른_roomRealtimeEventPublisher가_있으면_supabase_publisher를_추가로_등록하지_않는다() {
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
            context.register(빈선택지원설정.class, 대체퍼블리셔설정.class, RoomRealtimeEventPublisherConfiguration.class);
            context.refresh();

            assertThat(context.getBeansOfType(RoomRealtimeEventPublisher.class)).hasSize(1);
            assertThat(context.getBean(RoomRealtimeEventPublisher.class)).isSameAs(context.getBean("otherPublisher"));
        }
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "ROOM01",
            new TeamLeaderId("leader-host"),
            "호스트",
            "host-token",
            new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
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
        return new StartedRoomSnapshot(room, new GameFactory().create(snapshot));
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
        RoomRealtimeEventPublisher otherPublisher() {
            return new NoopRoomRealtimeEventPublisher();
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
