package com.naminhyeok.fantazzk.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import com.naminhyeok.fantazzk.room.support.RoomFixtureBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StartedGameContextLoaderTest {
    @Test
    void 시작된_게임_행위는_방의_액션_토큰으로_인증한다() {
        Room room = RoomFixtureBuilder.auction().started().buildRoom();
        Game game = RoomFixtureBuilder.gameFor(room);
        StartedGameContextLoader loader = new StartedGameContextLoader(
            new InMemoryRooms(Map.of(room.getId(), room)),
            new InMemoryGames(Map.of(game.getId(), game)),
            new RoomActionAuthorizer()
        );

        StartedGameActionContext action = loader.authenticate(game.getId().gameId(), RoomFixtureBuilder.HOST_TOKEN);

        assertThat(action.room().getId()).isEqualTo(room.getId());
        assertThat(action.game().getId()).isEqualTo(game.getId());
        assertThat(action.caller().getId()).isEqualTo(new TeamLeaderId(RoomFixtureBuilder.HOST_ID));
    }

    @Test
    void 시작된_게임이_없으면_GAME_NOT_FOUND를_반환한다() {
        StartedGameContextLoader loader =
            new StartedGameContextLoader(new InMemoryRooms(Map.of()), new InMemoryGames(Map.of()), new RoomActionAuthorizer());

        assertThatThrownBy(() -> loader.authenticate(UUID.randomUUID(), "any-token"))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.GAME_NOT_FOUND)
            );
    }

    @Test
    void 게임의_방이_없어도_GAME_NOT_FOUND를_반환한다() {
        Room room = RoomFixtureBuilder.auction().started().buildRoom();
        Game game = RoomFixtureBuilder.gameFor(room);
        StartedGameContextLoader loader =
            new StartedGameContextLoader(new InMemoryRooms(Map.of()), new InMemoryGames(Map.of(game.getId(), game)), new RoomActionAuthorizer());

        assertThatThrownBy(() -> loader.authenticate(game.getId().gameId(), "any-token"))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.GAME_NOT_FOUND)
            );
    }

    @Test
    void 액션_토큰이_유효하지_않으면_ROOM_ACTION_TOKEN_INVALID를_반환한다() {
        Room room = RoomFixtureBuilder.auction().started().buildRoom();
        Game game = RoomFixtureBuilder.gameFor(room);
        StartedGameContextLoader loader = new StartedGameContextLoader(
            new InMemoryRooms(Map.of(room.getId(), room)),
            new InMemoryGames(Map.of(game.getId(), game)),
            new RoomActionAuthorizer()
        );

        assertThatThrownBy(() -> loader.authenticate(game.getId().gameId(), "wrong-token"))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_ACTION_TOKEN_INVALID)
            );
    }

    private static final class InMemoryRooms implements Rooms {
        private final Map<RoomId, Room> byId;

        private InMemoryRooms(Map<RoomId, Room> rooms) {
            this.byId = new HashMap<>(rooms);
        }

        @Override
        public Room save(Room room) {
            byId.put(room.getId(), room);
            return room;
        }

        @Override
        public Room saveAndFlush(Room room) {
            byId.put(room.getId(), room);
            return room;
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return byId.values().stream().filter(room -> room.getCode().equals(code)).findFirst();
        }
    }

    private static final class InMemoryGames implements Games {
        private final Map<GameId, Game> byId;

        private InMemoryGames(Map<GameId, Game> games) {
            this.byId = new HashMap<>(games);
        }

        @Override
        public Game save(Game game) {
            byId.put(game.getId(), game);
            return game;
        }

        @Override
        public Optional<Game> findById(GameId id) {
            return Optional.ofNullable(byId.get(id));
        }
    }
}
