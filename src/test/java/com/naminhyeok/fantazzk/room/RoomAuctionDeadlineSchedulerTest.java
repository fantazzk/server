package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.mockito.InOrder;

class RoomAuctionDeadlineSchedulerTest {
    private static final Instant NOW = Instant.parse("2026-04-09T00:00:10Z");

    @Test
    void schedule는_경매_room_deadline에_맞춰_정산_task를_등록하고_다음_deadline을_재예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        Rooms rooms = mock(Rooms.class);
        Room room = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:15Z"));
        Room nextRoundRoom = auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:30Z"));
        given(settleAuction.settleIfDue("ROOM01")).willReturn(nextRoundRoom);
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                rooms,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        scheduler.schedule(room);

        assertThat(taskScheduler.scheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));

        taskScheduler.runLatest();

        verify(settleAuction).settleIfDue("ROOM01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:30Z"));
    }

    @Test
    void schedule는_같은_room의_기존_deadline_예약을_취소하고_새_deadline만_남긴다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        Rooms rooms = mock(Rooms.class);
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                rooms,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        scheduler.schedule(auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:15Z")));
        scheduler.schedule(auctionRoomWithDeadline("ROOM01", Instant.parse("2026-04-09T00:00:20Z")));

        assertThat(taskScheduler.cancelledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:20Z"));
    }

    @Test
    void 애플리케이션_시작시_due_room은_즉시_catch_up하고_future_deadline은_재예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        RecordingRooms rooms =
            new RecordingRooms(
                auctionRoomWithDeadline("DUE01", Instant.parse("2026-04-09T00:00:05Z")),
                auctionRoomWithDeadline("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"))
            );
        given(settleAuction.settleIfDue("DUE01")).willReturn(completedAuctionRoom("DUE01"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                rooms,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        scheduler.catchUpAndReschedule();

        verify(settleAuction).settleIfDue("DUE01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:15Z"));
    }

    @Test
    void 애플리케이션_시작시_null_deadline_legacy_room도_복구_대상에_포함한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        Room legacyRoom = auctionRoomWithDeadline("LEGACY01", Instant.parse("2026-04-09T00:00:15Z"));
        setCurrentAuctionRoundEndsAt(legacyRoom, null);
        RecordingRooms rooms = new RecordingRooms(legacyRoom);
        given(settleAuction.settleIfDue("LEGACY01"))
            .willReturn(auctionRoomWithDeadline("LEGACY01", Instant.parse("2026-04-09T00:00:25Z")));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                rooms,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        scheduler.catchUpAndReschedule();

        verify(settleAuction).settleIfDue("LEGACY01");
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(Instant.parse("2026-04-09T00:00:25Z"));
    }

    @Test
    void 애플리케이션_시작시_schedulable_room은_application_layer_정렬규칙으로_처리한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        Room legacyRoom = auctionRoomWithDeadline("LEGACY01", Instant.parse("2026-04-09T00:00:25Z"));
        setCurrentAuctionRoundEndsAt(legacyRoom, null);
        Room dueRoom = auctionRoomWithDeadline("DUE01", Instant.parse("2026-04-09T00:00:05Z"));
        Room futureRoom = auctionRoomWithDeadline("FUTURE01", Instant.parse("2026-04-09T00:00:15Z"));
        RecordingRooms rooms = new RecordingRooms(List.of(futureRoom, dueRoom, legacyRoom));
        given(settleAuction.settleIfDue("LEGACY01")).willReturn(auctionRoomWithDeadline("LEGACY01", Instant.parse("2026-04-09T00:00:20Z")));
        given(settleAuction.settleIfDue("DUE01")).willReturn(completedAuctionRoom("DUE01"));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                rooms,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        scheduler.catchUpAndReschedule();

        InOrder inOrder = inOrder(settleAuction);
        inOrder.verify(settleAuction).settleIfDue("LEGACY01");
        inOrder.verify(settleAuction).settleIfDue("DUE01");
        assertThat(taskScheduler.activeScheduledInstants())
            .containsExactly(
                Instant.parse("2026-04-09T00:00:20Z"),
                Instant.parse("2026-04-09T00:00:15Z")
            );
    }

    @Test
    void 애플리케이션_시작시_첫_페이지를_넘는_future_deadline도_모두_재예약한다() {
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        SettleAuction settleAuction = mock(SettleAuction.class);
        RecordingRooms rooms = new RecordingRooms(manyFutureRooms(205));
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                settleAuction,
                rooms,
                Clock.fixed(NOW, ZoneOffset.UTC)
            );

        scheduler.catchUpAndReschedule();

        assertThat(taskScheduler.activeScheduledInstants()).hasSize(205);
        assertThat(taskScheduler.activeScheduledInstants()).contains(
            Instant.parse("2026-04-09T00:01:00Z"),
            Instant.parse("2026-04-09T00:04:24Z")
        );
    }

    private static Room auctionRoomWithDeadline(String code, Instant deadline) {
        Room room =
            Room.createFromTemplate(
                code,
                new TeamLeaderId("host-" + code),
                "호스트",
                "host-token-" + code,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
                    2,
                    2,
                    300,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", 1)
                    )
                ),
                Instant.parse("2026-04-09T00:00:00Z")
            );
        room.join(new TeamLeaderId("guest-" + code), "게스트", "guest-token-" + code);
        room.start(new TeamLeaderId("host-" + code), Instant.parse("2026-04-09T00:00:00Z"));
        setCurrentAuctionRoundEndsAt(room, deadline);
        return room;
    }

    private static void setCurrentAuctionRoundEndsAt(Room room, Instant deadline) {
        try {
            var field = Room.class.getDeclaredField("currentAuctionRoundEndsAt");
            field.setAccessible(true);
            field.set(room, deadline);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Room completedAuctionRoom(String code) {
        Room room = auctionRoomWithDeadline(code, Instant.parse("2026-04-09T00:00:05Z"));
        setField(room, "status", RoomStatus.COMPLETED);
        setCurrentAuctionRoundEndsAt(room, null);
        return room;
    }

    private static void setField(Room room, String fieldName, Object value) {
        try {
            var field = Room.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(room, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static List<Room> manyFutureRooms(int count) {
        List<Room> rooms = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            int second = 60 + index;
            rooms.add(
                auctionRoomWithDeadline(
                    "ROOM%03d".formatted(index),
                    Instant.parse("2026-04-09T00:%02d:%02dZ".formatted(second / 60, second % 60))
                )
            );
        }
        return rooms;
    }

    private static final class RecordingRooms implements Rooms {
        private final List<Room> schedulableRooms;

        private RecordingRooms(Room... schedulableRooms) {
            this.schedulableRooms = List.of(schedulableRooms);
        }

        private RecordingRooms(List<Room> schedulableRooms) {
            this.schedulableRooms = List.copyOf(schedulableRooms);
        }

        @Override
        public Room save(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Room saveAndFlush(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.empty();
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.empty();
        }

        @Override
        public List<Room> findByStatusOrderByCreatedAtDescCodeDesc(RoomStatus status, Pageable pageable) {
            return List.of();
        }

        @Override
        public List<Room> findByStatusAndModeOrderByCodeAsc(RoomStatus status, RoomMode mode, Pageable pageable) {
            int start = Math.toIntExact(pageable.getOffset());
            if (start >= schedulableRooms.size()) {
                return List.of();
            }
            int end = Math.min(start + pageable.getPageSize(), schedulableRooms.size());
            return schedulableRooms.subList(start, end);
        }
    }
}
