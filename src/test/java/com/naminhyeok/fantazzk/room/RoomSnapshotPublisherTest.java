package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomSnapshotPublisherTest {
    private static final Instant STARTED_AT = Instant.parse("2026-04-15T00:00:00Z");

    @Test
    void 기본_publishAfterCommit_roomDetails는_started_game_state_유실을_막는다() {
        RoomSnapshotPublisher publisher = new RoomOnlyPublisher();

        assertThatThrownBy(() -> publisher.publishAfterCommit(startedDraftDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("game state");
    }

    private static RoomDetails startedDraftDetails() {
        Room room =
            Room.createFromTemplate(
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
                STARTED_AT
            );
        room.join(new TeamLeaderId("guest-1"), "게스트", "guest-action-token");
        room.selectDraftPosition(new TeamLeaderId("host-1"), 1);
        room.selectDraftPosition(new TeamLeaderId("guest-1"), 2);
        StartedGameSnapshot snapshot = room.start(
            new TeamLeaderId("host-1"),
            new GameId(UUID.fromString("00000000-0000-0000-0000-000000000101")),
            STARTED_AT
        );
        return new RoomDetails(room, new GameFactory().create(snapshot));
    }

    private static final class RoomOnlyPublisher implements RoomSnapshotPublisher {
        @Override
        public void publishAfterCommit(Room room) {
        }
    }
}
