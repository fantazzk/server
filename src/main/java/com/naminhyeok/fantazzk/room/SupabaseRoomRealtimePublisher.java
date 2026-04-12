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

class SupabaseRoomRealtimePublisher implements RoomRealtimePublisher {
    private static final Logger log = LoggerFactory.getLogger(SupabaseRoomRealtimePublisher.class);
    private static final String BROADCAST_URI = "/realtime/v1/api/broadcast";
    private static final String SNAPSHOT_UPDATED_EVENT = "snapshot.updated";

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
    public void publishAfterCommit(Room room) {
        PendingBroadcastSnapshot snapshot = PendingBroadcastSnapshot.from(room);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send(snapshot);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send(snapshot);
            }
        });
    }

    private void send(PendingBroadcastSnapshot snapshot) {
        RoomRealtimeSnapshotEvent event = snapshot.toEvent(Instant.now(clock));
        try {
            restClient.post()
                .uri(BROADCAST_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("apikey", serviceRoleKey)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                .body(new BroadcastRequest(List.of(new BroadcastMessage(topic(event.roomCode()), SNAPSHOT_UPDATED_EVENT, event))))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Supabase realtime broadcast publish failed. roomCode={}", event.roomCode(), ex);
        }
    }

    private String topic(String roomCode) {
        return topicPrefix + ":" + roomCode;
    }

    private record BroadcastRequest(List<BroadcastMessage> messages) {
    }

    private record BroadcastMessage(String topic, String event, RoomRealtimeSnapshotEvent payload) {
    }

    private record PendingBroadcastSnapshot(String roomCode, long snapshotVersion, RoomResponse room) {
        private static PendingBroadcastSnapshot from(Room room) {
            return new PendingBroadcastSnapshot(room.getCode(), room.getVersion(), RoomResponse.from(room));
        }

        private RoomRealtimeSnapshotEvent toEvent(Instant publishedAt) {
            return new RoomRealtimeSnapshotEvent("ROOM_SNAPSHOT_UPDATED", roomCode, snapshotVersion, publishedAt, room);
        }
    }
}

@Configuration(proxyBeanMethods = false)
class RoomRealtimePublisherConfiguration {
    @Bean
    @ConditionalOnMissingBean(RoomRealtimePublisher.class)
    @ConditionalOnExpression(
        "T(Boolean).parseBoolean('${fantazzk.supabase.realtime.enabled:false}') and " +
        "T(org.springframework.util.StringUtils).hasText('${fantazzk.supabase.url:}') and " +
        "T(org.springframework.util.StringUtils).hasText('${fantazzk.supabase.service-role-key:}')"
    )
    RoomRealtimePublisher supabaseRoomRealtimePublisher(
        RestClient.Builder restClientBuilder,
        Clock clock,
        @Value("${fantazzk.supabase.url}") String supabaseUrl,
        @Value("${fantazzk.supabase.service-role-key}") String serviceRoleKey,
        @Value("${fantazzk.supabase.realtime.topic-prefix:room}") String topicPrefix
    ) {
        return new SupabaseRoomRealtimePublisher(restClientBuilder, clock, supabaseUrl, serviceRoleKey, topicPrefix);
    }

    @Bean
    @ConditionalOnMissingBean(RoomRealtimePublisher.class)
    RoomRealtimePublisher noopRoomRealtimePublisher() {
        return new NoopRoomRealtimePublisher();
    }
}
