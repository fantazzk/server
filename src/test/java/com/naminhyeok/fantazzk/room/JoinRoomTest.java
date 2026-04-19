package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;

import com.naminhyeok.fantazzk.room.application.JoinRoom;
import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.application.TeamLeaderIdentityIssuer;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.infrastructure.realtime.NoopRoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JoinRoomTest {
    @Test
    void 저장후_팀장_순서가_바뀌어도_새로_참가한_팀장_세션을_반환한다() {
        Room room = RoomApiTestFixtures.waitingAuctionRoom();
        JoinRoom joinRoom =
            new JoinRoom(
                new ReorderingRooms(room),
                () -> new TeamLeaderIdentityIssuer.TeamLeaderIdentity("guest-joined", "guest-action-token"),
                new NoopRoomRealtimeEventPublisher()
            );

        RoomSessionResult joined = joinRoom.join(room.getCode(), "게스트");

        assertThat(joined.leader().getId()).isEqualTo(new TeamLeaderId("guest-joined"));
        assertThat(joined.leader().getActionToken()).isEqualTo("guest-action-token");
        assertThat(joined.room().getLeaders())
            .extracting(RoomTeamLeader::getId)
            .containsExactly(new TeamLeaderId("guest-joined"), new TeamLeaderId(RoomApiTestFixtures.HOST_ID));
    }

    private static final class ReorderingRooms implements Rooms {
        private final Room room;

        private ReorderingRooms(Room room) {
            this.room = room;
        }

        @Override
        public Room save(Room room) {
            return room;
        }

        @Override
        public Room saveAndFlush(Room room) {
            reverseLeaderOrder(room);
            return room;
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.of(room).filter(saved -> saved.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.of(room).filter(saved -> saved.getCode().equals(code));
        }

        @SuppressWarnings("unchecked")
        private static void reverseLeaderOrder(Room room) {
            try {
                Field field = Room.class.getDeclaredField("leaders");
                field.setAccessible(true);
                Collections.reverse((List<RoomTeamLeader>) field.get(room));
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            }
        }
    }
}
