package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class RoomRealtimePublishingTest {
    private static final Instant CREATED_AT = Instant.parse("2024-01-01T10:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2024-01-01T10:00:30Z");
    private static final String HOST_ID = "leader-host";
    private static final String HOST_ACTION_TOKEN = "host-token";
    private static final String GUEST_ID = "leader-guest";
    private static final String GUEST_ACTION_TOKEN = "guest-token";

    @Test
    void join은_저장된_room_스냅샷을_publish한다() {
        Room room = waitingAuctionRoom();
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        JoinRoom joinRoom = new JoinRoom(new SaveAndFlushOnlyRooms(room), fixedLeaderIdentityIssuer(), publisher);

        RoomSessionResult joined = joinRoom.join(room.getCode(), "게스트");

        assertThat(joined.leader().getId().value()).isEqualTo(GUEST_ID);
        assertThat(publisher.events).hasSize(1);
        assertPublishedRoom(publisher.events.getFirst(), room.getCode());
        assertThat(publisher.events.getFirst().room().teamLeaders()).hasSize(2);
    }

    @Test
    void join은_optimistic_lock을_room_concurrent_modification으로_번역한다() {
        Room room = waitingAuctionRoom();
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        JoinRoom joinRoom = new JoinRoom(new OptimisticLockFailureRooms(room), fixedLeaderIdentityIssuer(), publisher);

        assertThatThrownBy(() -> joinRoom.join(room.getCode(), "게스트"))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
        assertThat(publisher.events).isEmpty();
    }

    @Test
    void pickDraft는_저장된_room_스냅샷을_publish한다() {
        Room room = startedDraftRoom();
        DraftGame game = (DraftGame) gameFor(room);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        PickDraft pickDraft = new PickDraft(new InMemoryRooms(room), new InMemoryGames(game), new RoomActionAuthorizer(), publisher);

        RosterMember picked = pickDraft.pick(room.getCode(), HOST_ACTION_TOKEN, "선수1");

        assertThat(picked.playerName()).isEqualTo("선수1");
        assertThat(publisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
        assertPublishedRoom(event, room.getCode());
        assertThat(event.room().members()).hasSize(1);
        assertThat(event.room().progress().currentTurnIndex()).isEqualTo(1);
    }

    @Test
    void selectDraftPosition은_저장된_room_스냅샷을_publish한다() {
        Room room = waitingDraftRoomForPositionChange();
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        SelectDraftPosition selectDraftPosition =
            new SelectDraftPosition(new InMemoryRooms(room), new RoomActionAuthorizer(), publisher);

        selectDraftPosition.select(room.getCode(), HOST_ACTION_TOKEN, 1);

        assertThat(publisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
        assertPublishedRoom(event, room.getCode());
        assertThat(event.room().teamLeaders().getFirst().draftPosition()).isEqualTo(1);
    }

    @Test
    void clearDraftPosition은_저장된_room_스냅샷을_publish한다() {
        Room room = waitingDraftRoomForPositionChange();
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        ClearDraftPosition clearDraftPosition =
            new ClearDraftPosition(new InMemoryRooms(room), new RoomActionAuthorizer(), publisher);

        clearDraftPosition.clear(room.getCode(), HOST_ACTION_TOKEN);

        assertThat(publisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
        assertPublishedRoom(event, room.getCode());
        assertThat(event.room().teamLeaders().getFirst().draftPosition()).isNull();
    }

    @Test
    void settle는_저장된_latest_room_스냅샷을_publish한다() {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) gameFor(room);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        SettleAuctionAttempt settleAuctionAttempt =
            new SettleAuctionAttempt(new InMemoryRooms(room), new InMemoryGames(game), Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), publisher);

        AuctionSettlement settlement = settleAuctionAttempt.settle(room.getCode());

        assertThat(settlement).isEqualTo(new AuctionSettlement("선수1", AuctionOutcome.PASSED));
        assertThat(publisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
        assertPublishedRoom(event, room.getCode());
        assertThat(event.room().progress().currentRound()).isEqualTo(2);
    }

    @Test
    void settleIfDue는_기한이_지나면_정산된_latest_room_스냅샷을_publish한다() {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) gameFor(room);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        SettleAuctionAttempt settleAuctionAttempt =
            new SettleAuctionAttempt(new InMemoryRooms(room), new InMemoryGames(game), Clock.fixed(PUBLISHED_AT, ZoneOffset.UTC), publisher);

        Room settled = settleAuctionAttempt.settleIfDue(room.getCode());

        assertThat(settled.getStatus()).isEqualTo(RoomStatus.STARTED);
        assertThat(publisher.events).hasSize(1);
        RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
        assertPublishedRoom(event, room.getCode());
        assertThat(event.room().progress().currentRound()).isEqualTo(2);
        assertThat(event.room().progress().currentAuctionRoundEndsAt()).isEqualTo(PUBLISHED_AT.plusSeconds(30));
    }

    @Test
    void settleIfDue는_기한이_아직_아니면_publish하지_않는다() {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) gameFor(room);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        SettleAuctionAttempt settleAuctionAttempt =
            new SettleAuctionAttempt(new InMemoryRooms(room), new InMemoryGames(game), Clock.fixed(CREATED_AT.plusSeconds(10), ZoneOffset.UTC), publisher);

        Room current = settleAuctionAttempt.settleIfDue(room.getCode());

        assertThat(current.getStatus()).isEqualTo(RoomStatus.STARTED);
        assertThat(publisher.events).isEmpty();
    }

    private static TeamLeaderIdentityIssuer fixedLeaderIdentityIssuer() {
        return () -> new TeamLeaderIdentityIssuer.TeamLeaderIdentity(GUEST_ID, GUEST_ACTION_TOKEN);
    }

    private static void assertRoomError(CoreException exception, RoomErrorType errorType) {
        assertThat(exception.getError()).isSameAs(errorType);
    }

    private static void assertPublishedRoom(RoomRealtimeSnapshotEvent event, String roomCode) {
        assertThat(event.roomCode()).isEqualTo(roomCode);
        assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "ROOM01",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
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
            CREATED_AT
        );
    }

    private static Room startedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private static Room startedDraftRoom() {
        Room room = waitingDraftRoomForPositionChange();
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private static Room waitingDraftRoomForPositionChange() {
        Room room =
            Room.createFromTemplate(
                "DRF001",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        return room;
    }

    private static Game gameFor(Room room) {
        return new GameFactory().create(
            new StartedGameSnapshot(
                room.getId(),
                room.getCode(),
                room.getStartedGameId(),
                room.getStartedAt(),
                room.getMode(),
                new GameRules(
                    room.getTeamCount(),
                    room.getTeamSize(),
                    room.getBudget(),
                    room.getPickBanTime(),
                    room.getMinBidUnit(),
                    room.getPositionLimit(),
                    room.getDraftOrderStrategy()
                ),
                room.getLeaders().stream()
                    .map(leader -> new GameParticipant(leader.getId(), leader.getNickname(), leader.getDraftPosition(), leader.getRemainingBudget()))
                    .toList(),
                room.getPlayers().stream()
                    .map(player -> new GamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                    .toList()
            )
        );
    }

    private static final class RecordingRoomSnapshotPublisher implements RoomSnapshotPublisher {
        private final List<RoomRealtimeSnapshotEvent> events = new ArrayList<>();

        @Override
        public void publishAfterCommit(Room room) {
            events.add(RoomRealtimeSnapshotEvent.from(room, PUBLISHED_AT));
        }

        @Override
        public void publishAfterCommit(RoomDetails details) {
            events.add(RoomRealtimeSnapshotEvent.from(details, PUBLISHED_AT));
        }
    }

    private static final class SaveAndFlushOnlyRooms implements Rooms {
        private final Room room;

        private SaveAndFlushOnlyRooms(Room room) {
            this.room = room;
        }

        @Override
        public Room save(Room room) {
            throw new UnsupportedOperationException("JoinRoom should use saveAndFlush");
        }

        @Override
        public Room saveAndFlush(Room room) {
            return room;
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.ofNullable(room).filter(it -> it.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.ofNullable(room).filter(it -> it.getCode().equals(code));
        }
    }

    private static final class InMemoryRooms implements Rooms {
        private Room room;

        private InMemoryRooms(Room room) {
            this.room = room;
        }

        @Override
        public Room save(Room room) {
            this.room = room;
            return room;
        }

        @Override
        public Room saveAndFlush(Room room) {
            this.room = room;
            return room;
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.ofNullable(room).filter(it -> it.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.ofNullable(room).filter(it -> it.getCode().equals(code));
        }
    }

    private static final class InMemoryGames implements Games {
        private Game game;

        private InMemoryGames(Game game) {
            this.game = game;
        }

        @Override
        public Game save(Game game) {
            this.game = game;
            return game;
        }

        @Override
        public Optional<Game> findById(GameId id) {
            return Optional.ofNullable(game).filter(it -> it.getId().equals(id));
        }
    }

    private static final class OptimisticLockFailureRooms implements Rooms {
        private final Room room;

        private OptimisticLockFailureRooms(Room room) {
            this.room = room;
        }

        @Override
        public Room save(Room room) {
            throw new UnsupportedOperationException("JoinRoom should use saveAndFlush");
        }

        @Override
        public Room saveAndFlush(Room room) {
            throw new ObjectOptimisticLockingFailureException(Room.class, room.getId());
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.ofNullable(room).filter(it -> it.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.ofNullable(room).filter(it -> it.getCode().equals(code));
        }
    }
}
