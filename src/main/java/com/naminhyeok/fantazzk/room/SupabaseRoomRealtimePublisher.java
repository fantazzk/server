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
    private static final String ROOM_MEMBERSHIP_UPDATED = "room.membership.updated";
    private static final String ROOM_DRAFT_ORDER_UPDATED = "room.draft-order.updated";
    private static final String GAME_STARTED = "game.started";
    private static final String GAME_AUCTION_PROGRESS_UPDATED = "game.auction-progress.updated";
    private static final String GAME_DRAFT_PROGRESS_UPDATED = "game.draft-progress.updated";
    private static final String GAME_ROSTER_UPDATED = "game.roster.updated";

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
    public void publishRoomMembershipUpdatedAfterCommit(Room room) {
        publishAfterCommit(PendingRoomMembershipUpdated.from(room));
    }

    @Override
    public void publishRoomDraftOrderUpdatedAfterCommit(Room room) {
        publishAfterCommit(PendingRoomDraftOrderUpdated.from(room));
    }

    @Override
    public void publishGameStartedAfterCommit(StartedRoomSnapshot snapshot) {
        publishAfterCommit(PendingGameStarted.from(snapshot));
    }

    @Override
    public void publishGameAuctionProgressUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
        publishAfterCommit(PendingGameAuctionProgressUpdated.from(snapshot));
    }

    @Override
    public void publishGameDraftProgressUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
        publishAfterCommit(PendingGameDraftProgressUpdated.from(snapshot));
    }

    @Override
    public void publishGameRosterUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
        publishAfterCommit(PendingGameRosterUpdated.from(snapshot));
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

    private sealed interface PendingRealtimeEvent permits
        PendingRoomMembershipUpdated,
        PendingRoomDraftOrderUpdated,
        PendingGameStarted,
        PendingGameAuctionProgressUpdated,
        PendingGameDraftProgressUpdated,
        PendingGameRosterUpdated {

        String roomCode();

        String messageEvent();

        RoomRealtimeEvent toEvent(Instant publishedAt);
    }

    private record PendingRoomMembershipUpdated(String roomCode, long snapshotVersion, RoomMembershipProjection membership)
        implements PendingRealtimeEvent {
        private static PendingRoomMembershipUpdated from(Room room) {
            return new PendingRoomMembershipUpdated(room.getCode(), RoomRealtimeEventFactory.snapshotVersionOf(room), RoomMembershipProjection.from(room));
        }

        @Override
        public String messageEvent() {
            return ROOM_MEMBERSHIP_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new RoomMembershipUpdatedEvent("ROOM_MEMBERSHIP_UPDATED", roomCode, snapshotVersion, publishedAt, membership);
        }
    }

    private record PendingRoomDraftOrderUpdated(String roomCode, long snapshotVersion, RoomDraftOrderProjection draftOrder)
        implements PendingRealtimeEvent {
        private static PendingRoomDraftOrderUpdated from(Room room) {
            return new PendingRoomDraftOrderUpdated(room.getCode(), RoomRealtimeEventFactory.snapshotVersionOf(room), RoomDraftOrderProjection.from(room));
        }

        @Override
        public String messageEvent() {
            return ROOM_DRAFT_ORDER_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new RoomDraftOrderUpdatedEvent("ROOM_DRAFT_ORDER_UPDATED", roomCode, snapshotVersion, publishedAt, draftOrder);
        }
    }

    private record PendingGameStarted(String roomCode, long snapshotVersion, String gameId, GameStartProjection gameStart)
        implements PendingRealtimeEvent {
        private static PendingGameStarted from(StartedRoomSnapshot snapshot) {
            return new PendingGameStarted(
                snapshot.room().getCode(),
                RoomRealtimeEventFactory.snapshotVersionOf(snapshot),
                snapshot.game().getId().gameId().toString(),
                GameStartProjection.from(snapshot.game())
            );
        }

        @Override
        public String messageEvent() {
            return GAME_STARTED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new GameStartedEvent("GAME_STARTED", roomCode, snapshotVersion, publishedAt, gameId, gameStart);
        }
    }

    private record PendingGameAuctionProgressUpdated(String roomCode, long snapshotVersion, String gameId, AuctionProgressResponse auctionProgress)
        implements PendingRealtimeEvent {
        private static PendingGameAuctionProgressUpdated from(StartedRoomSnapshot snapshot) {
            return new PendingGameAuctionProgressUpdated(
                snapshot.room().getCode(),
                RoomRealtimeEventFactory.snapshotVersionOf(snapshot),
                snapshot.game().getId().gameId().toString(),
                AuctionProgressResponse.from(snapshot.game())
            );
        }

        @Override
        public String messageEvent() {
            return GAME_AUCTION_PROGRESS_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new GameAuctionProgressUpdatedEvent(
                "GAME_AUCTION_PROGRESS_UPDATED",
                roomCode,
                snapshotVersion,
                publishedAt,
                gameId,
                auctionProgress
            );
        }
    }

    private record PendingGameDraftProgressUpdated(String roomCode, long snapshotVersion, String gameId, DraftProgressResponse draftProgress)
        implements PendingRealtimeEvent {
        private static PendingGameDraftProgressUpdated from(StartedRoomSnapshot snapshot) {
            return new PendingGameDraftProgressUpdated(
                snapshot.room().getCode(),
                RoomRealtimeEventFactory.snapshotVersionOf(snapshot),
                snapshot.game().getId().gameId().toString(),
                DraftProgressResponse.from(snapshot.game())
            );
        }

        @Override
        public String messageEvent() {
            return GAME_DRAFT_PROGRESS_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new GameDraftProgressUpdatedEvent(
                "GAME_DRAFT_PROGRESS_UPDATED",
                roomCode,
                snapshotVersion,
                publishedAt,
                gameId,
                draftProgress
            );
        }
    }

    private record PendingGameRosterUpdated(String roomCode, long snapshotVersion, String gameId, GameRosterProjection roster)
        implements PendingRealtimeEvent {
        private static PendingGameRosterUpdated from(StartedRoomSnapshot snapshot) {
            return new PendingGameRosterUpdated(
                snapshot.room().getCode(),
                RoomRealtimeEventFactory.snapshotVersionOf(snapshot),
                snapshot.game().getId().gameId().toString(),
                GameRosterProjection.from(snapshot.game())
            );
        }

        @Override
        public String messageEvent() {
            return GAME_ROSTER_UPDATED;
        }

        @Override
        public RoomRealtimeEvent toEvent(Instant publishedAt) {
            return new GameRosterUpdatedEvent("GAME_ROSTER_UPDATED", roomCode, snapshotVersion, publishedAt, gameId, roster);
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
