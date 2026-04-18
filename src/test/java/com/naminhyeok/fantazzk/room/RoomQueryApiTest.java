package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.query.FindJoinableRooms;
import com.naminhyeok.fantazzk.room.application.query.JoinableRoomReader;
import com.naminhyeok.fantazzk.room.application.query.JoinableRoomSummary;
import com.naminhyeok.fantazzk.room.application.room.GetRoom;
import com.naminhyeok.fantazzk.room.domain.repository.Rooms;
import com.naminhyeok.fantazzk.room.domain.room.Room;
import com.naminhyeok.fantazzk.room.domain.shared.RoomId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RoomQueryApiTest {
    @Test
    void list는_query_summary를_root_view로_매핑한다() {
        JoinableRoomReader reader =
            limit -> List.of(
                new JoinableRoomSummary("ROOM01", "AUCTION", 2, 1, 1, "WAITING_FOR_LEADERS"),
                new JoinableRoomSummary("ROOM02", "DRAFT", 4, 3, 1, "WAITING_FOR_DRAFT_POSITIONS")
            );
        RoomQueryApi roomQueryApi = new ProvideRoomQueryApi(new GetRoom(new UnusedRooms()), new FindJoinableRooms(reader));

        assertThat(roomQueryApi.list())
            .extracting(JoinableRoomView::code, JoinableRoomView::mode, JoinableRoomView::remainingSlotCount, JoinableRoomView::startReadiness)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("ROOM01", "AUCTION", 1, "WAITING_FOR_LEADERS"),
                org.assertj.core.groups.Tuple.tuple("ROOM02", "DRAFT", 1, "WAITING_FOR_DRAFT_POSITIONS")
            );
    }

    private static final class UnusedRooms implements Rooms {
        @Override
        public Room save(Room room) {
            return room;
        }

        @Override
        public Room saveAndFlush(Room room) {
            return room;
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
}
