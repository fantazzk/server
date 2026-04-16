package com.naminhyeok.fantazzk.draft;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DraftRoomLifecycleTest {
    private static final String ROOM_CODE = "ROOM01";

    @Test
    void 공개_애플리케이션_서피스는_같은_저장소를_공유한다() {
        InMemoryDraftRooms draftRooms = new InMemoryDraftRooms();
        DraftRoomLifecycle lifecycle = new DraftRoomLifecycle(draftRooms);
        DraftRoomPlay play = new DraftRoomPlay(draftRooms);
        DraftRoomStateReader reader = new DraftRoomStateReader(draftRooms);

        DraftRoomState created = lifecycle.create(
            ROOM_CODE,
            2,
            2,
            DraftOrderStrategy.SNAKE,
            List.of(
                new DraftPlayerSpec(0, "선수1", "TOP", 0),
                new DraftPlayerSpec(1, "선수2", "JUNGLE", 1)
            )
        );
        lifecycle.addLeader(ROOM_CODE, "host-1", "호스트");
        lifecycle.addLeader(ROOM_CODE, "guest-1", "게스트");
        lifecycle.selectDraftPosition(ROOM_CODE, "host-1", 1);
        lifecycle.selectDraftPosition(ROOM_CODE, "guest-1", 2);
        DraftRoomState started = lifecycle.start(ROOM_CODE);
        DraftRoomState afterPick = play.pick(ROOM_CODE, "host-1", 0);
        DraftRoomState read = reader.read(ROOM_CODE);

        assertThat(created.readiness()).isEqualTo(DraftRoomReadiness.WAITING_FOR_LEADERS);
        assertThat(started.status()).isEqualTo(DraftRoomStatus.IN_PROGRESS);
        assertThat(afterPick.progress().currentTurnIndex()).isEqualTo(1);
        assertThat(read.members()).hasSize(1);
    }

    private static final class InMemoryDraftRooms implements DraftRooms {
        private final Map<DraftRoomId, DraftRoom> store = new LinkedHashMap<>();

        @Override
        public DraftRoom save(DraftRoom draftRoom) {
            store.put(draftRoom.getId(), draftRoom);
            return draftRoom;
        }

        @Override
        public Optional<DraftRoom> findById(DraftRoomId id) {
            return Optional.ofNullable(store.get(id));
        }
    }
}
