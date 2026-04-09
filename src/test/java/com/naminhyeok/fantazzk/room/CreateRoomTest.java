package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class CreateRoomTest {
    @Test
    void 방_코드_충돌이면_최대_세_번까지_새_방을_다시_만든다() {
        RecordingRooms rooms =
            new RecordingRooms(
                roomCodeCollision(),
                roomCodeCollision()
            );
        CreateRoom createRoom = new CreateRoom(rooms, new StubTemplateCatalog(), new StubTeamLeaderIdentityIssuer());

        Room created = createRoom.create(UUID.randomUUID(), "호스트");

        assertThat(created).isSameAs(rooms.savedRoom());
        assertThat(rooms.flushAttemptCount()).isEqualTo(3);
        assertThat(rooms.attemptedRoomIds()).doesNotHaveDuplicates();
    }

    @Test
    void 방_코드_충돌이_아닌_영속화_실패는_재시도하지_않고_그대로_전파한다() {
        DataIntegrityViolationException persistenceFailure =
            new DataIntegrityViolationException("different constraint");
        RecordingRooms rooms = new RecordingRooms(persistenceFailure);
        CreateRoom createRoom = new CreateRoom(rooms, new StubTemplateCatalog(), new StubTeamLeaderIdentityIssuer());

        assertThatThrownBy(() -> createRoom.create(UUID.randomUUID(), "호스트"))
            .isSameAs(persistenceFailure);
        assertThat(rooms.flushAttemptCount()).isEqualTo(1);
    }

    @Test
    void 방_코드_충돌이_세_번_연속이면_생성_실패_오류를_던진다() {
        RecordingRooms rooms =
            new RecordingRooms(
                roomCodeCollision(),
                roomCodeCollision(),
                roomCodeCollision()
            );
        CreateRoom createRoom = new CreateRoom(rooms, new StubTemplateCatalog(), new StubTeamLeaderIdentityIssuer());

        assertThatThrownBy(() -> createRoom.create(UUID.randomUUID(), "호스트"))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_CODE_GENERATION_FAILED);
                assertThat(coreException.getData()).isNull();
            });
        assertThat(rooms.flushAttemptCount()).isEqualTo(3);
    }

    private static DataIntegrityViolationException roomCodeCollision() {
        return new DataIntegrityViolationException("constraint [uk_rooms_code]");
    }

    private static final class RecordingRooms implements Rooms {
        private final Queue<RuntimeException> failures = new ArrayDeque<>();
        private final List<UUID> attemptedRoomIds = new ArrayList<>();
        private Room savedRoom;

        private RecordingRooms(RuntimeException... failures) {
            this.failures.addAll(List.of(failures));
        }

        @Override
        public Room save(Room room) {
            throw new UnsupportedOperationException("CreateRoom should use saveAndFlush");
        }

        @Override
        public Room saveAndFlush(Room room) {
            attemptedRoomIds.add(room.getId().roomId());
            RuntimeException failure = failures.poll();
            if (failure != null) {
                throw failure;
            }
            savedRoom = room;
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

        private Room savedRoom() {
            return savedRoom;
        }

        private int flushAttemptCount() {
            return attemptedRoomIds.size();
        }

        private List<UUID> attemptedRoomIds() {
            return attemptedRoomIds;
        }
    }

    private static final class StubTemplateCatalog implements TemplateCatalog {
        @Override
        public TemplateBlueprint getTemplate(UUID templateId) {
            return new TemplateBlueprint(
                templateId,
                Mode.AUCTION,
                2,
                2,
                300,
                null,
                List.of(new PlayerBlueprint("선수1", 1), new PlayerBlueprint("선수2", 2))
            );
        }
    }

    private static final class StubTeamLeaderIdentityIssuer implements TeamLeaderIdentityIssuer {
        @Override
        public TeamLeaderIdentity issue() {
            return new TeamLeaderIdentity("host-id", "action-token");
        }
    }
}
