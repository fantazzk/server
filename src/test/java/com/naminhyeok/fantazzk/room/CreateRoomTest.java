package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

class CreateRoomTest {
    @Test
    void 방_코드_충돌이면_최대_세_번까지_새_방을_다시_만든다() {
        RecordingRooms rooms =
            new RecordingRooms(
                roomCodeCollision(),
                decoratedRoomCodeCollision()
            );
        CreateRoom createRoom =
            new CreateRoom(
                new CreateRoomAttempt(rooms),
                new StubTemplateCatalog(),
                new StubTeamLeaderIdentityIssuer(),
                Clock.fixed(Instant.parse("2026-04-10T00:00:00Z"), ZoneOffset.UTC),
                new StubRoomCodeGenerator("ROOM01", "ROOM02", "ROOM03")
            );

        RoomSessionResult created = createRoom.create(UUID.randomUUID(), "호스트");

        assertThat(created.room()).isSameAs(rooms.savedRoom());
        assertThat(rooms.flushAttemptCount()).isEqualTo(3);
        assertThat(rooms.attemptedRoomIds()).doesNotHaveDuplicates();
        assertThat(created.room().getPickBanTime()).isEqualTo(45);
        assertThat(created.room().getMinBidUnit()).isEqualTo(10);
        assertThat(created.room().getPlayers().stream().map(RoomPlayer::getId))
            .containsExactly(new RoomPlayerId(0), new RoomPlayerId(1));
        assertThat(created.leader().getId()).isEqualTo(new TeamLeaderId("host-id"));
    }

    @Test
    void 방_코드_충돌이_아닌_영속화_실패는_재시도하지_않고_그대로_전파한다() {
        DataIntegrityViolationException persistenceFailure =
            new DataIntegrityViolationException(
                "constraint [uk_rooms_code]",
                new ConstraintViolationException("other constraint", null, "uk_rooms_host_id")
            );
        RecordingRooms rooms = new RecordingRooms(persistenceFailure);
        CreateRoom createRoom =
            new CreateRoom(
                new CreateRoomAttempt(rooms),
                new StubTemplateCatalog(),
                new StubTeamLeaderIdentityIssuer(),
                Clock.fixed(Instant.parse("2026-04-10T00:00:00Z"), ZoneOffset.UTC),
                new StubRoomCodeGenerator("ROOM01")
            );

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
        CreateRoom createRoom =
            new CreateRoom(
                new CreateRoomAttempt(rooms),
                new StubTemplateCatalog(),
                new StubTeamLeaderIdentityIssuer(),
                Clock.fixed(Instant.parse("2026-04-10T00:00:00Z"), ZoneOffset.UTC),
                new StubRoomCodeGenerator("ROOM01", "ROOM02", "ROOM03")
            );

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
        return new DataIntegrityViolationException(
            "insert failed",
            new ConstraintViolationException("duplicate key", null, "UK_ROOMS_CODE")
        );
    }

    private static DataIntegrityViolationException decoratedRoomCodeCollision() {
        return new DataIntegrityViolationException(
            "insert failed",
            new ConstraintViolationException(
                "duplicate key",
                null,
                "PUBLIC.UK_ROOMS_CODE INDEX PUBLIC.UK_ROOMS_CODE_INDEX_4"
            )
        );
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
                Mode.AUCTION,
                2,
                2,
                300,
                45,
                10,
                1,
                null,
                List.of(
                    new PlayerBlueprint("선수1", "TOP", 0),
                    new PlayerBlueprint("선수2", "JUNGLE", 1)
                )
            );
        }
    }

    private static final class StubTeamLeaderIdentityIssuer implements TeamLeaderIdentityIssuer {
        @Override
        public TeamLeaderIdentity issue() {
            return new TeamLeaderIdentity("host-id", "action-token");
        }
    }

    private static final class StubRoomCodeGenerator implements RoomCodeGenerator {
        private final Queue<String> roomCodes = new ArrayDeque<>();

        private StubRoomCodeGenerator(String... roomCodes) {
            this.roomCodes.addAll(List.of(roomCodes));
        }

        @Override
        public String generate() {
            return roomCodes.remove();
        }
    }
}
