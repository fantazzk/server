package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
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
    void 진행중인_경매_방_스냅샷은_퍼블리시_시점과_방_정보를_포함한다() {
        Room room = startedAuctionRoom();

        RoomRealtimeSnapshotEvent event = RoomRealtimeSnapshotEvent.from(room, PUBLISHED_AT);

        assertThat(event.eventType()).isEqualTo("ROOM_SNAPSHOT_UPDATED");
        assertThat(event.roomCode()).isEqualTo("AUC002");
        assertThat(event.snapshotVersion()).isEqualTo(room.getVersion());
        assertThat(event.publishedAt()).isEqualTo(PUBLISHED_AT);

        var json = OBJECT_MAPPER.valueToTree(event);

        assertThat(json.at("/room/code").asText()).isEqualTo("AUC002");
        assertThat(json.toString()).doesNotContain("actionToken");
        assertThat(json.at("/room/progress/currentAuctionTarget/name").asText()).isEqualTo("선수1");
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
}
