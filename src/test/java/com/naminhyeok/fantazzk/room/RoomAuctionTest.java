package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class RoomAuctionTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final int PICK_BAN_TIME = 45;
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_ACTION_TOKEN = "guest-action-token";

    @Test
    void 경매_방을_시작하면_1라운드_deadline이_생성된다() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);

        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);

        assertThat(room.getCurrentAuctionRound()).isEqualTo(1);
        assertThat(room.getCurrentAuctionRoundEndsAt()).isEqualTo(CREATED_AT.plusSeconds(PICK_BAN_TIME));
    }

    @Test
    void deadline_전에는_경매를_정산할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.settleAuction(CREATED_AT.plusSeconds(10)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_AUCTION_ROUND_NOT_ENDED));
    }

    @Test
    void deadline_가_지나면_입찰할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(PICK_BAN_TIME)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_REQUIRES_OPEN_ROUND));
    }

    @Test
    void 기존_경매_방의_deadline이_null이어도_입찰과_정산이_가능하다() throws Exception {
        Room room = startedAuctionRoom();
        setCurrentAuctionRoundEndsAt(room, null);
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));
        room.placeBid(guestLeaderId, 150, CREATED_AT.plusSeconds(2));

        AuctionSettlement settlement = room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME + 1L));

        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(room.getCurrentAuctionRound()).isEqualTo(2);
        assertThat(room.getCurrentAuctionRoundEndsAt()).isEqualTo(CREATED_AT.plusSeconds((PICK_BAN_TIME * 2L) + 1));
    }

    @Test
    void settleIfDue는_legacy_null_deadline을_복구하고_재예약한다() throws Exception {
        Room room = startedAuctionRoom();
        setCurrentAuctionRoundEndsAt(room, null);
        InMemoryRooms rooms = new InMemoryRooms(room);
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        RoomAuctionDeadlineScheduler scheduler =
            new RoomAuctionDeadlineScheduler(
                taskScheduler,
                unsupportedSettleAuction(),
                rooms,
                Clock.fixed(CREATED_AT, ZoneOffset.UTC)
            );
        SettleAuction settleAuction =
            new SettleAuction(
                rooms,
                Clock.fixed(CREATED_AT, ZoneOffset.UTC),
                new StubTransactionManager(),
                singletonProvider(scheduler)
            );

        Room repaired = settleAuction.settleIfDue(room.getCode());

        assertThat(repaired.getCurrentAuctionRoundEndsAt()).isEqualTo(CREATED_AT.plusSeconds(PICK_BAN_TIME));
        assertThat(taskScheduler.activeScheduledInstants()).containsExactly(CREATED_AT.plusSeconds(PICK_BAN_TIME));
    }

    @Test
    void 현재_최고가보다_낮거나_같은_입찰은_할_수_없다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));

        assertThatThrownBy(() -> room.placeBid(guestLeaderId, 100, CREATED_AT.plusSeconds(2)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_TOO_LOW));
    }

    @Test
    void 진행_중이_아니면_입찰할_수_없다() {
        Room room = waitingAuctionRoom();

        assertThatThrownBy(() -> room.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS));
    }

    @Test
    void 드래프트_방에서는_입찰할_수_없다() {
        Room room = startedDraftRoomForAuctionError();

        assertThatThrownBy(() -> room.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE));
    }

    @Test
    void 시작한_경매_방의_현재_라운드가_null이면_room_state_invalid를_던진다() throws Exception {
        Room room = startedAuctionRoom();
        setCurrentAuctionRound(room, null);

        assertThatThrownBy(() -> room.placeBid(new TeamLeaderId(HOST_ID), 100, CREATED_AT.plusSeconds(1)))
            .isInstanceOf(RoomStateInvalidException.class)
            .isInstanceOfSatisfying(RoomStateInvalidException.class, ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_STATE_INVALID));
    }

    @Test
    void 예산이_부족하면_입찰할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.placeBid(new TeamLeaderId(HOST_ID), 400, CREATED_AT.plusSeconds(1)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_BUDGET_EXCEEDED));
    }

    @Test
    void 입찰_금액은_0보다_커야_한다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();

        for (int amount : List.of(0, -1)) {
            assertThatThrownBy(() -> room.placeBid(hostLeaderId, amount, CREATED_AT.plusSeconds(1)))
                .isInstanceOf(CoreException.class)
                .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_AMOUNT_NOT_POSITIVE));
        }
    }

    @Test
    void 방에_없는_팀장_id로는_입찰할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.placeBid(new TeamLeaderId("unknown-leader"), 100, CREATED_AT.plusSeconds(1)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BIDDER_NOT_FOUND));
    }

    @Test
    void 낙찰되면_선수가_배정되고_예산이_차감되며_다음_라운드로_진행한다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        RoomBid firstBid = room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));
        RoomBid secondBid = room.placeBid(guestLeaderId, 150, CREATED_AT.plusSeconds(2));

        AuctionSettlement settlement = room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME));

        assertThat(firstBid.sequence()).isEqualTo(new BidSequence(1));
        assertThat(secondBid.sequence()).isEqualTo(new BidSequence(2));
        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(room.getMembers()).singleElement()
            .extracting(RoomTeamMember::teamLeaderId, RoomTeamMember::playerName)
            .containsExactly(guestLeaderId, "선수1");
        assertThat(room.getLeaders().stream().filter(it -> it.getId().equals(guestLeaderId)).findFirst().orElseThrow().getRemainingBudget())
            .isEqualTo(150);
        assertThat(room.getPlayers().getFirst().getStatus()).isEqualTo(PlayerStatus.ASSIGNED);
        assertThat(room.getCurrentAuctionRound()).isEqualTo(2);
        assertThat(room.getCurrentAuctionRoundEndsAt()).isEqualTo(CREATED_AT.plusSeconds(PICK_BAN_TIME * 2L));
    }

    @Test
    void 새_경매_라운드가_시작되면_입찰_순번은_다시_처음부터_시작한다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();

        room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));
        room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME));

        RoomBid nextRoundBid = room.placeBid(hostLeaderId, 120, CREATED_AT.plusSeconds(PICK_BAN_TIME + 1L));

        assertThat(nextRoundBid.sequence()).isEqualTo(new BidSequence(1));
        assertThat(nextRoundBid.round()).isEqualTo(2);
    }

    @Test
    void 모든_선수를_배정하면_deadline이_사라진다() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);

        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));
        room.placeBid(guestLeaderId, 150, CREATED_AT.plusSeconds(2));
        room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME));

        room.placeBid(hostLeaderId, 120, CREATED_AT.plusSeconds(PICK_BAN_TIME + 1L));
        room.placeBid(guestLeaderId, 130, CREATED_AT.plusSeconds(PICK_BAN_TIME + 2L));
        room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME * 2L));

        assertThat(room.getStatus()).isEqualTo(RoomStatus.COMPLETED);
        assertThat(room.getCurrentAuctionRoundEndsAt()).isNull();
    }

    @Test
    void 진행_중이_아니면_낙찰_처리를_할_수_없다() {
        Room room = waitingAuctionRoom();

        assertThatThrownBy(() -> room.settleAuction(CREATED_AT))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS));
    }

    @Test
    void 드래프트_방에서는_낙찰_처리를_할_수_없다() {
        Room room = startedDraftRoomForAuctionError();

        assertThatThrownBy(() -> room.settleAuction(CREATED_AT))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE));
    }

    @Test
    void 시작한_경매_방의_현재_라운드가_null이면_낙찰_처리도_room_state_invalid를_던진다() throws Exception {
        Room room = startedAuctionRoom();
        setCurrentAuctionRound(room, null);

        assertThatThrownBy(() -> room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME)))
            .isInstanceOf(RoomStateInvalidException.class)
            .isInstanceOfSatisfying(RoomStateInvalidException.class, ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_STATE_INVALID));
    }

    @Test
    void 낙찰_팀장의_남은_예산이_null로_손상되면_정산은_room_state_invalid를_던진다() throws Exception {
        Room room = startedAuctionRoom();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(guestLeaderId, 150, CREATED_AT.plusSeconds(1));
        setRemainingBudget(room, guestLeaderId, null);

        assertThatThrownBy(() -> room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME)))
            .isInstanceOf(RoomStateInvalidException.class)
            .isInstanceOfSatisfying(RoomStateInvalidException.class, ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_STATE_INVALID));
    }

    @Test
    void 낙찰_팀장의_남은_예산이_입찰가보다_작게_손상되면_정산은_room_state_invalid를_던진다() throws Exception {
        Room room = startedAuctionRoom();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(guestLeaderId, 150, CREATED_AT.plusSeconds(1));
        setRemainingBudget(room, guestLeaderId, 100);

        assertThatThrownBy(() -> room.settleAuction(CREATED_AT.plusSeconds(PICK_BAN_TIME)))
            .isInstanceOf(RoomStateInvalidException.class)
            .isInstanceOfSatisfying(RoomStateInvalidException.class, ex -> assertThat(ex.getError()).isEqualTo(RoomErrorType.ROOM_STATE_INVALID));
    }

    @Test
    void start는_optimistic_lock을_room_conflict로_번역한다() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        InMemoryRooms rooms = new InMemoryRooms(room);
        rooms.failOnSave(new ObjectOptimisticLockingFailureException(Room.class, room.getId()));
        StartRoom startRoom =
            new StartRoom(
                rooms,
                new RoomActionAuthorizer(),
                new RoomAuctionDeadlineScheduler(
                    new FakeTaskScheduler(),
                    unsupportedSettleAuction(),
                    rooms,
                    Clock.fixed(CREATED_AT, ZoneOffset.UTC)
                ),
                Clock.fixed(CREATED_AT, ZoneOffset.UTC),
                new StubTransactionManager()
            );

        assertThatThrownBy(() -> startRoom.start(room.getCode(), HOST_ACTION_TOKEN))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    @Test
    void placeBid는_optimistic_lock을_room_conflict로_번역한다() {
        Room room = startedAuctionRoom();
        InMemoryRooms rooms = new InMemoryRooms(room);
        rooms.failOnSave(new ObjectOptimisticLockingFailureException(Room.class, room.getId()));
        PlaceBid placeBid =
            new PlaceBid(
                rooms,
                new RoomActionAuthorizer(),
                new RoomAuctionDeadlineScheduler(
                    new FakeTaskScheduler(),
                    unsupportedSettleAuction(),
                    rooms,
                    Clock.fixed(CREATED_AT, ZoneOffset.UTC)
                ),
                Clock.fixed(CREATED_AT.plusSeconds(1), ZoneOffset.UTC),
                new StubTransactionManager()
            );

        assertThatThrownBy(() -> placeBid.place(room.getCode(), HOST_ACTION_TOKEN, 100))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    @Test
    void pickDraft는_optimistic_lock을_room_conflict로_번역한다() {
        Room room = inProgressDraftRoomForOptimisticLock();
        InMemoryRooms rooms = new InMemoryRooms(room);
        rooms.failOnSave(new ObjectOptimisticLockingFailureException(Room.class, room.getId()));
        PickDraft pickDraft = new PickDraft(rooms, new RoomActionAuthorizer(), new StubTransactionManager());

        assertThatThrownBy(() -> pickDraft.pick(room.getCode(), HOST_ACTION_TOKEN, "선수1"))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    private static void assertRoomError(CoreException ex, RoomErrorType expected) {
        assertThat(ex.getError()).isEqualTo(expected);
    }

    private static void setCurrentAuctionRoundEndsAt(Room room, Instant value) throws Exception {
        var field = Room.class.getDeclaredField("currentAuctionRoundEndsAt");
        field.setAccessible(true);
        field.set(room, value);
    }

    private static void setCurrentAuctionRound(Room room, Integer value) throws Exception {
        var field = Room.class.getDeclaredField("currentAuctionRound");
        field.setAccessible(true);
        field.set(room, value);
    }

    private static void setRemainingBudget(Room room, TeamLeaderId leaderId, Integer value) throws Exception {
        RoomTeamLeader leader =
            room.getLeaders().stream()
                .filter(it -> it.getId().equals(leaderId))
                .findFirst()
                .orElseThrow();
        var field = RoomTeamLeader.class.getDeclaredField("remainingBudget");
        field.setAccessible(true);
        field.set(leader, value);
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "ROOM01",
            new TeamLeaderId(HOST_ID),
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
                2,
                2,
                300,
                PICK_BAN_TIME,
                null,
                List.of(
                    new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                    new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                )
            ),
            CREATED_AT
        );
    }

    private static Room startedDraftRoomForAuctionError() {
        Room room =
            Room.createFromTemplate(
                "ROOM02",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private static Room startedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private static Room inProgressDraftRoomForOptimisticLock() {
        Room room =
            Room.createFromTemplate(
                "ROOM03",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        room.selectDraftPosition(new TeamLeaderId(GUEST_ID), 2);
        room.start(new TeamLeaderId(HOST_ID), CREATED_AT);
        return room;
    }

    private static SettleAuction unsupportedSettleAuction() {
        return new SettleAuction(
            new InMemoryRooms(),
            Clock.fixed(CREATED_AT, ZoneOffset.UTC),
            new StubTransactionManager(),
            new ObjectProvider<>() {
                @Override
                public RoomAuctionDeadlineScheduler getObject(Object... args) {
                    return null;
                }

                @Override
                public RoomAuctionDeadlineScheduler getObject() {
                    return null;
                }

                @Override
                public RoomAuctionDeadlineScheduler getIfAvailable() {
                    return null;
                }

                @Override
                public RoomAuctionDeadlineScheduler getIfUnique() {
                    return null;
                }
            }
        ) {
            @Override
            public Room settleIfDue(String code) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static ObjectProvider<RoomAuctionDeadlineScheduler> singletonProvider(RoomAuctionDeadlineScheduler scheduler) {
        return new ObjectProvider<>() {
            @Override
            public RoomAuctionDeadlineScheduler getObject(Object... args) {
                return scheduler;
            }

            @Override
            public RoomAuctionDeadlineScheduler getObject() {
                return scheduler;
            }

            @Override
            public RoomAuctionDeadlineScheduler getIfAvailable() {
                return scheduler;
            }

            @Override
            public RoomAuctionDeadlineScheduler getIfUnique() {
                return scheduler;
            }
        };
    }

    private static final class InMemoryRooms implements Rooms {
        private Room room;
        private RuntimeException saveFailure;

        private InMemoryRooms() {}

        private InMemoryRooms(Room room) {
            this.room = room;
        }

        private void failOnSave(RuntimeException saveFailure) {
            this.saveFailure = saveFailure;
        }

        @Override
        public Room save(Room room) {
            if (saveFailure != null) {
                throw saveFailure;
            }
            this.room = room;
            return room;
        }

        @Override
        public Room saveAndFlush(Room room) {
            return save(room);
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.ofNullable(room).filter(it -> it.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.ofNullable(room).filter(it -> it.getCode().equals(code));
        }

        @Override
        public List<Room> findByStatusOrderByCreatedAtDescCodeDesc(RoomStatus status, Pageable pageable) {
            return List.of();
        }

        @Override
        public List<Room> findByStatusAndModeOrderByCodeAsc(RoomStatus status, RoomMode mode, Pageable pageable) {
            return Optional.ofNullable(room).stream().toList();
        }
    }

    private static final class StubTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {}

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {}
    }
}
