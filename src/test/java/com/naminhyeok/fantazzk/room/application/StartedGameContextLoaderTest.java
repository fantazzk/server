package com.naminhyeok.fantazzk.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.RoomActionAuthorizer;
import com.naminhyeok.fantazzk.room.application.StartedGameActionContext;
import com.naminhyeok.fantazzk.room.application.StartedGameContextLoader;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.nio.charset.StandardCharsets;
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

        assertThatThrownBy(() -> loader.authenticate(UUID.randomUUID(), "any-token"))
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

        assertThatThrownBy(() -> loader.authenticate(game.getId().gameId(), "any-token"))
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
                    "LEAGUE_OF_LEGENDS",
                    RoomMode.AUCTION,
                    2,
                    2,
                    300,
                    45,
                    10,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
        );
        room.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        room.start(new TeamLeaderId("host-1"), deterministicGameId(room), CREATED_AT);
        return room;
    }

    private Game startedGameOf(Room room) {
        return new GameFactory().create(
            new StartedGameSnapshot(
                room.getId(),
                room.getCode(),
                room.getStartedGameId(),
                room.getStartedAt(),
                room.getGameType(),
                room.getMode(),
                GameRules.auction(room.getTeamCount(), room.getTeamSize(), room.getBudget(), room.getPickBanTime(), room.getMinBidUnit()),
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

    private static GameId deterministicGameId(Room room) {
        String source = "game:%s".formatted(room.getId().roomId());
        return new GameId(UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)));
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
