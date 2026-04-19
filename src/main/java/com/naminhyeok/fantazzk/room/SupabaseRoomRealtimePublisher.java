package com.naminhyeok.fantazzk.room;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class SupabaseRoomRealtimePublisher implements RoomRealtimeEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(SupabaseRoomRealtimePublisher.class);
    private static final String BROADCAST_URI = "/realtime/v1/api/broadcast";
    private static final String ROOM_UPDATED = "room.updated";
    private static final String GAME_UPDATED = "game.updated";

    private final RestClient restClient;
    private final Clock clock;
    private final String serviceRoleKey;
    private final String topicPrefix;

    SupabaseRoomRealtimePublisher(
        RestClient.Builder restClientBuilder,
        Clock clock,
        String supabaseUrl,
        String serviceRoleKey,
        String topicPrefix
    ) {
        this(restClientBuilder.baseUrl(supabaseUrl), clock, serviceRoleKey, topicPrefix);
    }

    SupabaseRoomRealtimePublisher(RestClient.Builder restClientBuilder, Clock clock, String serviceRoleKey, String topicPrefix) {
        this(restClientBuilder.build(), clock, serviceRoleKey, topicPrefix);
    }

    private SupabaseRoomRealtimePublisher(RestClient restClient, Clock clock, String serviceRoleKey, String topicPrefix) {
        this.restClient = restClient;
        this.clock = clock;
        this.serviceRoleKey = serviceRoleKey;
        this.topicPrefix = topicPrefix;
    }

    @Override
    public void publishRoomUpdatedAfterCommit(Room room) {
        publishAfterCommit(PendingRoomUpdated.from(room));
    }

    @Override
    public void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
        publishAfterCommit(PendingGameUpdated.from(snapshot));
    }

    private void publishAfterCommit(PendingRealtimeEvent pendingEvent) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send(pendingEvent);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send(pendingEvent);
            }
        });
    }

    private void send(PendingRealtimeEvent pendingEvent) {
        RoomRealtimeEvent event = pendingEvent.toEvent(Instant.now(clock));
        try {
            restClient.post()
                .uri(BROADCAST_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("apikey", serviceRoleKey)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                .body(new BroadcastRequest(List.of(new BroadcastMessage(topic(event.roomCode()), pendingEvent.messageEvent(), event))))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Supabase realtime broadcast publish failed. roomCode={}", pendingEvent.roomCode(), ex);
        }
    }

    private String topic(String roomCode) {
        return topicPrefix + ":" + roomCode;
    }

    private record BroadcastRequest(List<BroadcastMessage> messages) {
    }

    private record BroadcastMessage(String topic, String event, RoomRealtimeEvent payload) {
    }

    private sealed interface PendingRealtimeEvent permits PendingRoomUpdated, PendingGameUpdated {

        String roomCode();

        String messageEvent();

        RoomRealtimeEvent toEvent(Instant publishedAt);
    }

    private record PendingRoomUpdated(String roomCode, long snapshotVersion, RoomDetailResponse room)
        implements PendingRealtimeEvent {
        private static PendingRoomUpdated from(Room room) {
            return new PendingRoomUpdated(room.getCode(), RoomRealtimeEventFactory.snapshotVersionOf(room), RoomDetailResponse.from(room));
        }

        @Override
        public String messageEvent() {
            return ROOM_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new RoomUpdatedEvent("ROOM_UPDATED", roomCode, snapshotVersion, publishedAt, room);
        }
    }

    private record PendingGameUpdated(String roomCode, long snapshotVersion, GameDetailResponse game)
        implements PendingRealtimeEvent {
        private static PendingGameUpdated from(StartedRoomSnapshot snapshot) {
            return new PendingGameUpdated(
                snapshot.room().getCode(),
                RoomRealtimeEventFactory.snapshotVersionOf(snapshot),
                GameDetailResponse.from(snapshot.game())
            );
        }

        @Override
        public String messageEvent() {
            return GAME_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new GameUpdatedEvent("GAME_UPDATED", roomCode, snapshotVersion, publishedAt, game);
        }
    }
}

@Configuration(proxyBeanMethods = false)
class RoomRealtimeEventPublisherConfiguration {
    @Bean
    @ConditionalOnMissingBean(RoomRealtimeEventPublisher.class)
    @ConditionalOnExpression(
        "T(Boolean).parseBoolean('${fantazzk.supabase.realtime.enabled:false}') and " +
        "T(org.springframework.util.StringUtils).hasText('${fantazzk.supabase.url:}') and " +
        "T(org.springframework.util.StringUtils).hasText('${fantazzk.supabase.service-role-key:}')"
    )
    RoomRealtimeEventPublisher supabaseRoomSnapshotPublisher(
        RestClient.Builder restClientBuilder,
        Clock clock,
        @Value("${fantazzk.supabase.url}") String supabaseUrl,
        @Value("${fantazzk.supabase.service-role-key}") String serviceRoleKey,
        @Value("${fantazzk.supabase.realtime.topic-prefix:room}") String topicPrefix
    ) {
        return new SupabaseRoomRealtimePublisher(restClientBuilder, clock, supabaseUrl, serviceRoleKey, topicPrefix);
    }

    @Bean
    @ConditionalOnMissingBean(RoomRealtimeEventPublisher.class)
    RoomRealtimeEventPublisher noopRoomRealtimeEventPublisher() {
        return new NoopRoomRealtimeEventPublisher();
    }
}
