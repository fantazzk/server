package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import com.naminhyeok.fantazzk.room.GameView;
import com.naminhyeok.fantazzk.room.RoomView;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class SupabaseRoomRealtimePublisher implements RoomSnapshotPublisher {
    private static final Logger log = LoggerFactory.getLogger(SupabaseRoomRealtimePublisher.class);
    private static final String BROADCAST_URI = "/realtime/v1/api/broadcast";
    private static final String SNAPSHOT_UPDATED_EVENT = "snapshot.updated";

    private final RestClient restClient;
    private final Clock clock;
    private final String serviceRoleKey;
    private final String topicPrefix;

    public SupabaseRoomRealtimePublisher(
        RestClient.Builder restClientBuilder,
        Clock clock,
        String supabaseUrl,
        String serviceRoleKey,
        String topicPrefix
    ) {
        this(restClientBuilder.baseUrl(supabaseUrl), clock, serviceRoleKey, topicPrefix);
    }

    public SupabaseRoomRealtimePublisher(RestClient.Builder restClientBuilder, Clock clock, String serviceRoleKey, String topicPrefix) {
        this(restClientBuilder.build(), clock, serviceRoleKey, topicPrefix);
    }

    private SupabaseRoomRealtimePublisher(RestClient restClient, Clock clock, String serviceRoleKey, String topicPrefix) {
        this.restClient = restClient;
        this.clock = clock;
        this.serviceRoleKey = serviceRoleKey;
        this.topicPrefix = topicPrefix;
    }

    @Override
    public void publishAfterCommit(RoomSnapshot snapshot) {
        publishAfterCommit(PendingBroadcastSnapshot.from(snapshot));
    }

    @Override
    public void publishAfterCommit(StartedRoomSnapshot snapshot) {
        publishAfterCommit(PendingBroadcastSnapshot.from(snapshot));
    }

    private void publishAfterCommit(PendingBroadcastSnapshot snapshot) {
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
        RealtimeSnapshotEvent event = snapshot.toEvent(Instant.now(clock));
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

    private record BroadcastMessage(String topic, String event, RealtimeSnapshotEvent payload) {
    }

    private record PendingBroadcastSnapshot(String roomCode, long snapshotVersion, RoomView room, GameView game) {
        private static PendingBroadcastSnapshot from(RoomSnapshot snapshot) {
            return new PendingBroadcastSnapshot(snapshot.roomCode(), snapshot.snapshotVersion(), snapshot.room(), null);
        }

        private static PendingBroadcastSnapshot from(StartedRoomSnapshot snapshot) {
            return new PendingBroadcastSnapshot(snapshot.roomCode(), snapshot.snapshotVersion(), null, snapshot.game());
        }

        private RealtimeSnapshotEvent toEvent(Instant publishedAt) {
            if (game == null) {
                return new RoomSnapshotUpdatedEvent(
                    "ROOM_SNAPSHOT_UPDATED",
                    roomCode,
                    snapshotVersion,
                    publishedAt,
                    room
                );
            }
            return new GameSnapshotUpdatedEvent(
                "GAME_SNAPSHOT_UPDATED",
                roomCode,
                snapshotVersion,
                publishedAt,
                game
            );
        }
    }
}
