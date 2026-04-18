package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.query.FindJoinableRooms;
import com.naminhyeok.fantazzk.room.application.query.JoinableRoomReader;
import com.naminhyeok.fantazzk.room.application.query.JoinableRoomSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

class FindJoinableRoomsTest {
    @Test
    void query_summary를_root_view로_매핑한다() {
        JoinableRoomReader reader =
            limit -> List.of(
                new JoinableRoomSummary("ROOM01", "AUCTION", 2, 1, 1, "WAITING_FOR_LEADERS"),
                new JoinableRoomSummary("ROOM02", "DRAFT", 4, 3, 1, "WAITING_FOR_DRAFT_POSITIONS")
            );
        FindJoinableRooms findJoinableRooms = new FindJoinableRooms(reader);

        assertThat(findJoinableRooms.list())
            .extracting(JoinableRoomView::code, JoinableRoomView::mode, JoinableRoomView::remainingSlotCount, JoinableRoomView::startReadiness)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("ROOM01", "AUCTION", 1, "WAITING_FOR_LEADERS"),
                org.assertj.core.groups.Tuple.tuple("ROOM02", "DRAFT", 1, "WAITING_FOR_DRAFT_POSITIONS")
            );
    }
}
