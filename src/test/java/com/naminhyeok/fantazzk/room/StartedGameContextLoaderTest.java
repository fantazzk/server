package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.support.RoomActionAuthorizer;
import com.naminhyeok.fantazzk.room.application.support.StartedGameContextLoader;
import com.naminhyeok.fantazzk.room.domain.game.Game;
import com.naminhyeok.fantazzk.room.domain.game.GameFactory;
import com.naminhyeok.fantazzk.room.domain.handoff.GameRules;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedAuctionParticipant;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedGamePlayer;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.repository.Games;
import com.naminhyeok.fantazzk.room.domain.repository.Rooms;
import com.naminhyeok.fantazzk.room.domain.room.Room;
import com.naminhyeok.fantazzk.room.domain.shared.GameId;
import com.naminhyeok.fantazzk.room.domain.shared.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.shared.RoomId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StartedGameContextLoaderTest {
    @Test
    void 기존_game이_가리키는_room이_없으면_GAME_NOT_FOUND를_반환한다() {
        Room room = RoomApiTestFixtures.startedAuctionRoom();
        Game game = startedAuctionGameOf(room);
        StartedGameContextLoader loader =
            new StartedGameContextLoader(new MissingRooms(), new SingleGameRepository(game), new RoomActionAuthorizer());

        assertThatThrownBy(() -> loader.load(game.getId().gameId()))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.GAME_NOT_FOUND)
            );
    }

    @Test
    void uuid기반_started_game_인증에서_잘못된_actionToken이면_ROOM_ACTION_TOKEN_INVALID를_반환한다() {
        Room room = RoomApiTestFixtures.startedAuctionRoom();
        Game game = startedAuctionGameOf(room);
        StartedGameContextLoader loader =
            new StartedGameContextLoader(new SingleRoomRepository(room), new SingleGameRepository(game), new RoomActionAuthorizer());

        assertThatThrownBy(() -> loader.authenticate(game.getId().gameId(), "wrong-token"))
            .isInstanceOfSatisfying(
                CoreException.class,
                ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_ACTION_TOKEN_INVALID)
            );
    }

    private static Game startedAuctionGameOf(Room room) {
        return new GameFactory().create(
            new StartedGameSnapshot(
                room.getId(),
                room.getCode(),
                room.getStartedGameId(),
                room.getStartedAt(),
                GameRules.auction(
                    room.getTeamCount(),
                    room.getTeamSize(),
                    room.getBudget(),
                    room.getPickBanTime(),
                    room.getMinBidUnit(),
                    room.getPositionLimit()
                ),
                room.getLeaders().stream()
                    .map(leader -> new StartedAuctionParticipant(leader.getId(), leader.getNickname(), leader.getRemainingBudget()))
                    .toList(),
                room.getPlayers().stream()
                    .map(player -> new StartedGamePlayer(player.getId(), player.getName(), player.getPosition(), player.getDisplayOrder()))
                    .toList()
            )
        );
    }

    private static final class MissingRooms implements Rooms {
        @Override
        public Room save(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Room saveAndFlush(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.empty();
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.empty();
        }
    }

    private static final class SingleRoomRepository implements Rooms {
        private final Room room;

        private SingleRoomRepository(Room room) {
            this.room = room;
        }

        @Override
        public Room save(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Room saveAndFlush(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.of(room).filter(saved -> saved.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.of(room).filter(saved -> saved.getCode().equals(code));
        }
    }

    private static final class SingleGameRepository implements Games {
        private final Game game;

        private SingleGameRepository(Game game) {
            this.game = game;
        }

        @Override
        public Game save(Game game) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Game> findById(GameId id) {
            return Optional.of(game).filter(saved -> saved.getId().equals(id));
        }
    }
}
