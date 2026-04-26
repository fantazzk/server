package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameParticipant;
import com.naminhyeok.fantazzk.room.domain.GamePlayer;
import com.naminhyeok.fantazzk.room.domain.GameRules;
import com.naminhyeok.fantazzk.room.domain.GameStatus;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.GameUpdatedEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomRealtimeEventFactory;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomUpdatedEvent;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomRealtimeEventFactoryTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2026-04-09T00:10:00Z");

    @Test
    void 로비_갱신_이벤트는_로비_화면_스냅샷을_담는다() {
        Room room = waitingAuctionRoom();

        RoomRealtimeEvent event = RoomRealtimeEventFactory.roomUpdated(room, PUBLISHED_AT);

        assertThat(event).isInstanceOf(RoomUpdatedEvent.class);
        RoomUpdatedEvent roomUpdated = (RoomUpdatedEvent) event;
        assertThat(roomUpdated.eventType()).isEqualTo("ROOM_UPDATED");
        assertThat(roomUpdated.roomCode()).isEqualTo(room.getCode());
        assertThat(roomUpdated.room().roomCode()).isEqualTo(room.getCode());
        assertThat(roomUpdated.room().status()).isEqualTo(RoomStatus.WAITING.name());
        assertThat(roomUpdated.room().leaders()).hasSize(1);
        assertThat(roomUpdated.room().playerPool()).hasSize(2);
    }

    @Test
    void 게임_갱신_이벤트는_진행_화면_스냅샷을_담는다() {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);

        RoomRealtimeEvent event = RoomRealtimeEventFactory.gameUpdated(new StartedRoomSnapshot(room, game), PUBLISHED_AT);

        assertThat(event).isInstanceOf(GameUpdatedEvent.class);
        GameUpdatedEvent gameUpdated = (GameUpdatedEvent) event;
        assertThat(gameUpdated.eventType()).isEqualTo("GAME_UPDATED");
        assertThat(gameUpdated.roomCode()).isEqualTo(room.getCode());
        assertThat(gameUpdated.game().gameId()).isEqualTo(game.getId().gameId().toString());
        assertThat(gameUpdated.game().status()).isEqualTo(GameStatus.IN_PROGRESS.name());
        assertThat(gameUpdated.game().roster()).isEmpty();
        assertThat(gameUpdated.game().auctionProgress().currentRound()).isEqualTo(1);
    }

    private Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "AUC002",
            new TeamLeaderId("host-1"),
            "호스트",
            "host-action-token",
            new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
                RoomMode.AUCTION,
                2,
                2,
                300,
                15,
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

    private Room startedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId("guest-1"), "게스트1", "guest-action-token");
        room.start(new TeamLeaderId("host-1"), deterministicGameId(room), CREATED_AT);
        return room;
    }

    private Game startedGameOf(Room room) {
        StartedGameSnapshot snapshot = new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            room.getStartedAt(),
            room.getGameType(),
            room.getMode(),
            GameRules.auction(room.getTeamCount(), room.getTeamSize(), room.getBudget(), room.getPickBanTime(), room.getMinBidUnit()),
            List.of(
                GameParticipant.auction(new TeamLeaderId("host-1"), "호스트", 300),
                GameParticipant.auction(new TeamLeaderId("guest-1"), "게스트1", 300)
            ),
            List.of(
                new GamePlayer(new RoomPlayerId(0), "선수1", "TOP", 0),
                new GamePlayer(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
            )
        );
        return new GameFactory().create(snapshot);
    }

    private static com.naminhyeok.fantazzk.room.domain.GameId deterministicGameId(Room room) {
        String source = "game:%s".formatted(room.getId().roomId());
        return new com.naminhyeok.fantazzk.room.domain.GameId(
            UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8))
        );
    }
}
