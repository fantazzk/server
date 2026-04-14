package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetRoomDetailsTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-15T00:00:00Z");

    @Test
    void 시작되지_않은_방은_room만_반환한다() {
        Room room = waitingDraftRoom();
        GetRoomDetails getRoomDetails = new GetRoomDetails(new SingleRoomRooms(room), new EmptyGames());

        RoomDetails details = getRoomDetails.get(room.getCode());

        assertThat(details.room()).isSameAs(room);
        assertThat(details.game()).isNull();
    }

    @Test
    void 시작된_드래프트_방에서_game이_없으면_room_state_invalid를_던진다() {
        Room room = startedDraftRoom();
        GetRoomDetails getRoomDetails = new GetRoomDetails(new SingleRoomRooms(room), new EmptyGames());

        assertThatThrownBy(() -> getRoomDetails.get(room.getCode()))
            .isInstanceOf(RoomStateInvalidException.class)
            .isInstanceOfSatisfying(
                RoomStateInvalidException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_STATE_INVALID)
            );
    }

    @Test
    void 시작된_방에서_startedGameId가_없으면_room_state_invalid를_던진다() throws Exception {
        Room room = startedDraftRoom();
        var field = Room.class.getDeclaredField("startedGameId");
        field.setAccessible(true);
        field.set(room, null);
        GetRoomDetails getRoomDetails = new GetRoomDetails(new SingleRoomRooms(room), new EmptyGames());

        assertThatThrownBy(() -> getRoomDetails.get(room.getCode()))
            .isInstanceOf(RoomStateInvalidException.class)
            .isInstanceOfSatisfying(
                RoomStateInvalidException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_STATE_INVALID)
            );
    }

    @Test
    void 시작된_드래프트_방은_저장된_game을_반환한다() {
        Room room = startedDraftRoom();
        DraftGame game = startedDraftGame(room);
        GetRoomDetails getRoomDetails = new GetRoomDetails(new SingleRoomRooms(room), new SingleGameGames(game));

        RoomDetails details = getRoomDetails.get(room.getCode());

        assertThat(details.room()).isSameAs(room);
        assertThat(details.game()).isSameAs(game);
    }

    private static Room waitingDraftRoom() {
        return Room.createFromTemplate(
            "DRF001",
            new TeamLeaderId("host-1"),
            "호스트",
            "host-action-token",
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
    }

    private static Room startedDraftRoom() {
        Room room = waitingDraftRoom();
        room.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        room.selectDraftPosition(new TeamLeaderId("host-1"), 1);
        room.selectDraftPosition(new TeamLeaderId("guest-1"), 2);
        room.start(new TeamLeaderId("host-1"), CREATED_AT);
        return room;
    }

    private static DraftGame startedDraftGame(Room room) {
        Room source = waitingDraftRoom();
        source.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        source.selectDraftPosition(new TeamLeaderId("host-1"), 1);
        source.selectDraftPosition(new TeamLeaderId("guest-1"), 2);
        StartedGameSnapshot snapshot = source.start(
            new TeamLeaderId("host-1"),
            room.getStartedGameId(),
            CREATED_AT
        );
        return (DraftGame) new GameFactory().create(snapshot);
    }

    private record SingleRoomRooms(Room room) implements Rooms {
        @Override
        public Room save(Room room) {
            return room;
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

    private record SingleGameGames(Game game) implements Games {
        @Override
        public Game save(Game game) {
            return game;
        }

        @Override
        public Optional<Game> findById(GameId id) {
            return Optional.ofNullable(game).filter(it -> it.getId().equals(id));
        }
    }

    private static final class EmptyGames implements Games {
        @Override
        public Game save(Game game) {
            return game;
        }

        @Override
        public Optional<Game> findById(GameId id) {
            return Optional.empty();
        }
    }
}
