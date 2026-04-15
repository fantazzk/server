package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomRealtimeSnapshotEventTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2026-04-09T00:10:00Z");
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_ACTION_TOKEN = "guest-action-token";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void 시작된_방_스냅샷은_game이_없으면_room_라이브_진행정보를_포함하지_않는다() {
        Room room = startedAuctionRoom();

        RoomRealtimeSnapshotEvent event = RoomRealtimeSnapshotEvent.from(RoomDetails.from(room), PUBLISHED_AT);

        assertThat(event.eventType()).isEqualTo("ROOM_SNAPSHOT_UPDATED");
        assertThat(event.roomCode()).isEqualTo("AUC002");
        assertThat(event.snapshotVersion()).isEqualTo(room.getVersion());
        assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);

        var json = OBJECT_MAPPER.valueToTree(event);

        assertThat(json.at("/room/code").asText()).isEqualTo("AUC002");
        assertThat(json.at("/room/status").asText()).isEqualTo("STARTED");
        assertThat(json.toString()).doesNotContain("actionToken");
        assertThat(json.at("/room/progress/currentRound").isNull()).isTrue();
        assertThat(json.at("/room/progress/currentAuctionTarget").isNull()).isTrue();
    }

    @Test
    void 진행중인_경매_방_스냅샷은_Game의_라이브_필드를_우선한다() {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) startedGame();
        game.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(1));
        game.settleAuction(CREATED_AT.plusSeconds(46));

        RoomRealtimeSnapshotEvent event = RoomRealtimeSnapshotEvent.from(new RoomDetails(room, game), PUBLISHED_AT);

        var json = OBJECT_MAPPER.valueToTree(event);

        assertThat(json.at("/room/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(json.at("/room/teamLeaders/0/remainingBudget").asInt()).isEqualTo(200);
        assertThat(json.at("/room/players/0/status").asText()).isEqualTo("ASSIGNED");
        assertThat(json.at("/room/members/0/playerName").asText()).isEqualTo("선수1");
        assertThat(json.at("/room/progress/currentRound").asInt()).isEqualTo(2);
        assertThat(json.at("/room/progress/currentAuctionTarget/name").asText()).isEqualTo("선수2");
    }

    @Test
    void 진행중인_경매_방_스냅샷_version은_live_game_version을_우선한다() throws Exception {
        Room room = startedAuctionRoom();
        AuctionGame game = (AuctionGame) startedGame();
        setVersion(Room.class, room, 1L);
        setVersion(Game.class, game, 7L);

        RoomRealtimeSnapshotEvent event = RoomRealtimeSnapshotEvent.from(new RoomDetails(room, game), PUBLISHED_AT);

        assertThat(event.snapshotVersion()).isEqualTo(8L);
    }

    private static void setVersion(Class<?> owner, Object target, long version) throws Exception {
        var field = owner.getDeclaredField("version");
        field.setAccessible(true);
        field.setLong(target, version);
    }

    private Room startedAuctionRoom() {
        Room room =
            Room.createFromTemplate(
                "AUC002",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
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
        room.join(new TeamLeaderId(GUEST_ID), "게스트1", GUEST_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private Game startedGame() {
        Room room =
            Room.createFromTemplate(
                "AUC002",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
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
        room.join(new TeamLeaderId(GUEST_ID), "게스트1", GUEST_ACTION_TOKEN);
        StartedGameSnapshot snapshot = room.start(
            new TeamLeaderId(HOST_ID),
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
            CREATED_AT
        );
        return new GameFactory().create(snapshot);
    }
}
