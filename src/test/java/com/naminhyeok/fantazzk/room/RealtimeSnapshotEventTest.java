package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.GameSnapshotUpdatedEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RealtimeSnapshotEvent;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.RoomSnapshotUpdatedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RealtimeSnapshotEventTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2026-04-09T00:10:00Z");

    @Test
    void waiting_snapshot은_room_payload를_담고_game_payload는_비운다() {
        Room room = waitingAuctionRoom();

        RealtimeSnapshotEvent event = RealtimeSnapshotEvent.from(
            new RoomSnapshot(room.getCode(), room.getVersion(), RoomView.from(room)),
            PUBLISHED_AT
        );

        assertThat(event).isInstanceOf(RoomSnapshotUpdatedEvent.class);
        assertThat(event.eventType()).isEqualTo("ROOM_SNAPSHOT_UPDATED");
        assertThat(event.roomCode()).isEqualTo(room.getCode());
        assertThat(event.room()).isNotNull();
        assertThat(event.room().code()).isEqualTo(room.getCode());
        assertThat(event.game()).isNull();
    }

    @Test
    void started_snapshot은_game_payload를_담고_room_payload는_비운다() {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);

        RealtimeSnapshotEvent event = RealtimeSnapshotEvent.from(
            new StartedRoomSnapshot(room.getCode(), room.getVersion() + game.getVersion(), GameView.from(game)),
            PUBLISHED_AT
        );

        assertThat(event).isInstanceOf(GameSnapshotUpdatedEvent.class);
        assertThat(event.eventType()).isEqualTo("GAME_SNAPSHOT_UPDATED");
        assertThat(event.roomCode()).isEqualTo(room.getCode());
        assertThat(event.room()).isNull();
        assertThat(event.game()).isNotNull();
        assertThat(event.game().id()).isEqualTo(game.getId().gameId().toString());
        assertThat(event.game().roomCode()).isEqualTo(room.getCode());
    }

    @Test
    void started_snapshot_version은_room과_game_version을_합산한다() throws Exception {
        Room room = startedAuctionRoom();
        Game game = startedGameOf(room);
        setVersion(Room.class, room, 1L);
        setVersion(Game.class, game, 7L);

        RealtimeSnapshotEvent event = RealtimeSnapshotEvent.from(
            new StartedRoomSnapshot(room.getCode(), room.getVersion() + game.getVersion(), GameView.from(game)),
            PUBLISHED_AT
        );

        assertThat(event.snapshotVersion()).isEqualTo(8L);
    }

    private Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "AUC002",
            new TeamLeaderId("host-1"),
            "호스트",
            "host-action-token",
            new RoomTemplateSpec(
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
        room.start(new TeamLeaderId("host-1"), CREATED_AT);
        return room;
    }

    private Game startedGameOf(Room room) {
        StartedGameSnapshot snapshot = new StartedGameSnapshot(
            room.getId(),
            room.getCode(),
            room.getStartedGameId(),
            room.getStartedAt(),
            room.getMode(),
            GameRules.auction(room.getTeamCount(), room.getTeamSize(), room.getBudget(), room.getPickBanTime(), room.getMinBidUnit(), room.getPositionLimit()),
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

    private static void setVersion(Class<?> owner, Object target, long version) throws Exception {
        var field = owner.getDeclaredField("version");
        field.setAccessible(true);
        field.setLong(target, version);
    }
}
