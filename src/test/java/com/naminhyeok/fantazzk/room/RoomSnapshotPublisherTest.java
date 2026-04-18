package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomSnapshotPublisherTest {
    private static final Instant STARTED_AT = Instant.parse("2026-04-15T00:00:00Z");

    @Test
    void 기본_publishAfterCommit_roomDetails는_started_game_state_유실을_막는다() {
        RoomSnapshotPublisher publisher = new RoomOnlyPublisher();

        assertThatThrownBy(() -> publisher.publishAfterCommit(startedDraftSnapshot()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("game state");
    }

    private static StartedRoomSnapshot startedDraftSnapshot() {
        Room room =
            Room.createFromTemplate(
                "DRF001",
                new TeamLeaderId("host-1"),
                "호스트",
                "host-action-token",
                new RoomTemplateSpec(
                    RoomMode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    DraftOrderStrategy.SNAKE,
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
        Game game = new GameFactory().create(snapshot);
        return new StartedRoomSnapshot(room.getCode(), room.getVersion() + game.getVersion(), GameView.from(game));
    }

    private static final class RoomOnlyPublisher implements RoomSnapshotPublisher {
        @Override
        public void publishAfterCommit(RoomSnapshot snapshot) {
        }

        @Override
        public void publishAfterCommit(StartedRoomSnapshot snapshot) {
            throw new IllegalStateException("started room snapshot must preserve game state");
        }
    }
}
