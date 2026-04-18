package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StartedGameContextLoaderTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-18T00:00:00Z");

    @Test
    void gameId로_started_game_action_context를_읽고_room_기반으로_인증한다() {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);
        StartedGameContextLoader loader = new StartedGameContextLoader(
            new InMemoryRooms(Map.of(room.getId(), room)),
            new InMemoryGames(Map.of(game.getId(), game)),
            new RoomActionAuthorizer()
        );

        StartedGameActionContext action = loader.authenticate(game.getId().gameId(), "host-action-token");

        assertThat(action.room().getId()).isEqualTo(room.getId());
        assertThat(action.game().getId()).isEqualTo(game.getId());
        assertThat(action.caller().getId()).isEqualTo(new TeamLeaderId("host-1"));
    }

    @Test
    void 없는_gameId면_GAME_NOT_FOUND를_반환한다() {
        StartedGameContextLoader loader =
            new StartedGameContextLoader(new InMemoryRooms(Map.of()), new InMemoryGames(Map.of()), new RoomActionAuthorizer());

        assertThatThrownBy(() -> loader.load(UUID.randomUUID()))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.GAME_NOT_FOUND)
            );
    }

    @Test
    void room이_없어도_GAME_NOT_FOUND를_반환한다() {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);
        StartedGameContextLoader loader =
            new StartedGameContextLoader(new InMemoryRooms(Map.of()), new InMemoryGames(Map.of(game.getId(), game)), new RoomActionAuthorizer());

        assertThatThrownBy(() -> loader.load(game.getId().gameId()))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.GAME_NOT_FOUND)
            );
    }

    @Test
    void actionToken이_유효하지_않으면_room_auth_error를_반환한다() {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);
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

    private Room startedAuctionRoom() {
        Room room =
            Room.createFromTemplate(
                "ROOM01",
                new TeamLeaderId("host-1"),
                "호스트",
                "host-action-token",
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
                    2,
                    2,
                    300,
                    45,
                    10,
                    1,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        room.start(new TeamLeaderId("host-1"), CREATED_AT);
        return room;
    }

    private Game startedGameOf(Room room) {
        return new GameFactory().create(
            new StartedGameSnapshot(
                room.getId(),
                room.getCode(),
                room.getStartedGameId(),
                room.getStartedAt(),
                room.getMode(),
                GameRules.auction(room.getTeamCount(), room.getTeamSize(), room.getBudget(), room.getPickBanTime(), room.getMinBidUnit(), room.getPositionLimit()),
                List.of(
                    GameParticipant.auction(new TeamLeaderId("host-1"), "호스트", 300),
                    GameParticipant.auction(new TeamLeaderId("guest-1"), "게스트", 300)
                ),
                List.of(
                    new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            )
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
