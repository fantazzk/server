package com.naminhyeok.fantazzk.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.ClearDraftPosition;
import com.naminhyeok.fantazzk.room.application.JoinRoom;
import com.naminhyeok.fantazzk.room.application.RoomActionAuthorizer;
import com.naminhyeok.fantazzk.room.application.RoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.application.SelectDraftPosition;
import com.naminhyeok.fantazzk.room.application.SettleAuctionAttempt;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEventFactory;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomUpdatedEvent;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.room.support.RoomFixtureBuilder;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class RoomRealtimePublishingTest {
    private static final Instant CREATED_AT = RoomFixtureBuilder.CREATED_AT;
    private static final Instant PUBLISHED_AT = Instant.parse("2024-01-01T10:00:30Z");

    @Test
    void 방_참가_저장_충돌은_동시_수정_오류로_번역하고_이벤트를_발행하지_않는다() {
        Room room = RoomFixtureBuilder.auction().buildRoom();
        RecordingRoomRealtimeEventPublisher publisher = new RecordingRoomRealtimeEventPublisher();
        JoinRoom joinRoom = new JoinRoom(new OptimisticLockFailureRooms(room), publisher);

        assertThatThrownBy(() -> joinRoom.join(room.getCode(), "게스트"))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
        assertThat(publisher.events).isEmpty();
    }

    @Test
    void 드래프트_자리_선택은_로비_스냅샷을_발행한다() {
        Room room = RoomFixtureBuilder.draft().joined().buildRoom();
        RecordingRoomRealtimeEventPublisher publisher = new RecordingRoomRealtimeEventPublisher();
        SelectDraftPosition selectDraftPosition =
            new SelectDraftPosition(new InMemoryRooms(room), new RoomActionAuthorizer(), publisher);

        selectDraftPosition.select(room.getCode(), RoomFixtureBuilder.HOST_TOKEN, 1);

        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(RoomUpdatedEvent.class);
        RoomUpdatedEvent event = (RoomUpdatedEvent) publisher.events.getFirst();
        assertThat(event.roomCode()).isEqualTo(room.getCode());
        assertThat(event.room().draftOrder().slots().getFirst().leaderId()).isEqualTo(RoomFixtureBuilder.HOST_ID);
    }

    @Test
    void 드래프트_자리_취소는_로비_스냅샷을_발행한다() {
        Room room = RoomFixtureBuilder.draft().joined().buildRoom();
        room.selectDraftPosition(new TeamLeaderId(RoomFixtureBuilder.HOST_ID), 1);
        RecordingRoomRealtimeEventPublisher publisher = new RecordingRoomRealtimeEventPublisher();
        ClearDraftPosition clearDraftPosition =
            new ClearDraftPosition(new InMemoryRooms(room), new RoomActionAuthorizer(), publisher);

        clearDraftPosition.clear(room.getCode(), RoomFixtureBuilder.HOST_TOKEN);

        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(RoomUpdatedEvent.class);
        RoomUpdatedEvent event = (RoomUpdatedEvent) publisher.events.getFirst();
        assertThat(event.room().draftOrder().slots().getFirst().leaderId()).isNull();
    }

    @Test
    void 마감_전_정산은_스냅샷을_발행하지_않는다() {
        Room room = RoomFixtureBuilder.auction().started().buildRoom();
        AuctionGame game = (AuctionGame) RoomFixtureBuilder.gameFor(room);
        RecordingRoomRealtimeEventPublisher publisher = new RecordingRoomRealtimeEventPublisher();
        InMemoryRooms rooms = new InMemoryRooms(room);
        InMemoryGames games = new InMemoryGames(game);
        SettleAuctionAttempt settleAuctionAttempt =
            new SettleAuctionAttempt(
                rooms,
                games,
                Clock.fixed(CREATED_AT.plusSeconds(10), ZoneOffset.UTC),
                publisher
            );

        settleAuctionAttempt.settleIfDue(room.getCode());

        assertThat(publisher.events).isEmpty();
    }

    private static void assertRoomError(CoreException exception, RoomErrorType errorType) {
        assertThat(exception.getError()).isSameAs(errorType);
    }

    private static final class RecordingRoomRealtimeEventPublisher implements RoomRealtimeEventPublisher {
        private final List<RoomRealtimeEvent> events = new ArrayList<>();

        @Override
        public void publishRoomUpdatedAfterCommit(Room room) {
            events.add(RoomRealtimeEventFactory.roomUpdated(room, PUBLISHED_AT));
        }

        @Override
        public void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
            events.add(RoomRealtimeEventFactory.gameUpdated(snapshot, PUBLISHED_AT));
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
