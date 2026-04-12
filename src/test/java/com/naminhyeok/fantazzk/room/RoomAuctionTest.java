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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class RoomAuctionTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
    private static final int PICK_BAN_TIME = 45;
    private static final int MIN_BID_UNIT = 10;
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
    void 첫_입찰은_최소_입찰_단위_이상이어야_한다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();

        assertThatThrownBy(() -> room.placeBid(hostLeaderId, 5, CREATED_AT.plusSeconds(1)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_MIN_UNIT_NOT_MET));
    }

    @Test
    void 이후_입찰은_현재_최고가보다_최소_입찰_단위만큼_높아야_한다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));

        assertThatThrownBy(() -> room.placeBid(guestLeaderId, 105, CREATED_AT.plusSeconds(2)))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_MIN_UNIT_NOT_MET));
    }

    @Test
    void 최소_입찰_단위를_만족하면_다음_입찰을_할_수_있다() {
        Room room = startedAuctionRoom();
        TeamLeaderId hostLeaderId = room.getLeaders().getFirst().getId();
        TeamLeaderId guestLeaderId = room.getLeaders().getLast().getId();

        room.placeBid(hostLeaderId, 100, CREATED_AT.plusSeconds(1));

        RoomBid bid = room.placeBid(guestLeaderId, 110, CREATED_AT.plusSeconds(2));

        assertThat(bid.amount()).isEqualTo(110);
        assertThat(bid.sequence()).isEqualTo(new BidSequence(2));
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
    void start는_저장후_realtime_publish하고_deadline_scheduling은_afterCommit에_유지한다() {
        Room room = waitingAuctionRoom();
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        InMemoryRooms rooms = new InMemoryRooms(room);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        StartRoom startRoom =
            new StartRoom(
                rooms,
                new RoomActionAuthorizer(),
                new RoomAuctionDeadlineScheduler(
                    taskScheduler,
                    unsupportedSettleAuction(),
                    emptyAuctionScheduleReader(),
                    Clock.fixed(CREATED_AT, ZoneOffset.UTC)
                ),
                publisher,
                Clock.fixed(CREATED_AT, ZoneOffset.UTC)
            );

        TransactionSynchronizationManager.initSynchronization();
        try {
            startRoom.start(room.getCode(), HOST_ACTION_TOKEN);

            assertThat(publisher.events).hasSize(1);
            RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
            assertThat(event.roomCode()).isEqualTo(room.getCode());
            assertThat(event.snapshotVersion()).isEqualTo(1L);
            assertThat(event.room().progress().currentRound()).isEqualTo(1);
            assertThat(event.room().progress().currentAuctionRoundEndsAt()).isEqualTo(CREATED_AT.plusSeconds(PICK_BAN_TIME));
            assertThat(taskScheduler.scheduledInstants()).isEmpty();

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            TransactionSynchronizationManager.clearSynchronization();
            synchronizations.forEach(TransactionSynchronization::afterCommit);
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }

        assertThat(taskScheduler.scheduledInstants()).containsExactly(CREATED_AT.plusSeconds(PICK_BAN_TIME));
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
                    emptyAuctionScheduleReader(),
                    Clock.fixed(CREATED_AT, ZoneOffset.UTC)
                ),
                noopRoomSnapshotPublisher(),
                Clock.fixed(CREATED_AT, ZoneOffset.UTC)
            );

        assertThatThrownBy(() -> startRoom.start(room.getCode(), HOST_ACTION_TOKEN))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    @Test
    void placeBid는_저장후_realtime_publish하고_deadline_scheduling은_afterCommit에_유지한다() {
        Room room = startedAuctionRoom();
        InMemoryRooms rooms = new InMemoryRooms(room);
        RecordingRoomSnapshotPublisher publisher = new RecordingRoomSnapshotPublisher();
        FakeTaskScheduler taskScheduler = new FakeTaskScheduler();
        PlaceBid placeBid =
            new PlaceBid(
                rooms,
                new RoomActionAuthorizer(),
                new RoomAuctionDeadlineScheduler(
                    taskScheduler,
                    unsupportedSettleAuction(),
                    emptyAuctionScheduleReader(),
                    Clock.fixed(CREATED_AT, ZoneOffset.UTC)
                ),
                publisher,
                Clock.fixed(CREATED_AT.plusSeconds(1), ZoneOffset.UTC)
            );

        TransactionSynchronizationManager.initSynchronization();
        try {
            RoomBid bid = placeBid.place(room.getCode(), HOST_ACTION_TOKEN, 100);

            assertThat(bid.amount()).isEqualTo(100);
            assertThat(publisher.events).hasSize(1);
            RoomRealtimeSnapshotEvent event = publisher.events.getFirst();
            assertThat(event.roomCode()).isEqualTo(room.getCode());
            assertThat(event.snapshotVersion()).isEqualTo(1L);
            assertThat(event.room().progress().currentRound()).isEqualTo(1);
            assertThat(event.room().progress().highestBidAmount()).isEqualTo(100);
            assertThat(event.room().progress().bidCount()).isEqualTo(1);
            assertThat(taskScheduler.scheduledInstants()).isEmpty();

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            TransactionSynchronizationManager.clearSynchronization();
            synchronizations.forEach(TransactionSynchronization::afterCommit);
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }

        assertThat(taskScheduler.scheduledInstants()).containsExactly(CREATED_AT.plusSeconds(PICK_BAN_TIME));
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
                    emptyAuctionScheduleReader(),
                    Clock.fixed(CREATED_AT, ZoneOffset.UTC)
                ),
                noopRoomSnapshotPublisher(),
                Clock.fixed(CREATED_AT.plusSeconds(1), ZoneOffset.UTC)
            );

        assertThatThrownBy(() -> placeBid.place(room.getCode(), HOST_ACTION_TOKEN, 100))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    @Test
    void pickDraft는_optimistic_lock을_room_conflict로_번역한다() {
        Room room = inProgressDraftRoomForOptimisticLock();
        InMemoryRooms rooms = new InMemoryRooms(room);
        rooms.failOnSave(new ObjectOptimisticLockingFailureException(Room.class, room.getId()));
        PickDraft pickDraft = new PickDraft(rooms, new RoomActionAuthorizer(), noopRoomSnapshotPublisher());

        assertThatThrownBy(() -> pickDraft.pick(room.getCode(), HOST_ACTION_TOKEN, "선수1"))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    @Test
    void selectDraftPosition은_optimistic_lock을_room_conflict로_번역한다() {
        Room room = waitingDraftRoomForPositionChange();
        InMemoryRooms rooms = new InMemoryRooms(room);
        rooms.failOnSave(new ObjectOptimisticLockingFailureException(Room.class, room.getId()));
        SelectDraftPosition selectDraftPosition = new SelectDraftPosition(
            rooms,
            new RoomActionAuthorizer(),
            noopRoomSnapshotPublisher()
        );

        assertThatThrownBy(() -> selectDraftPosition.select(room.getCode(), HOST_ACTION_TOKEN, 1))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    @Test
    void clearDraftPosition은_optimistic_lock을_room_conflict로_번역한다() {
        Room room = waitingDraftRoomForPositionChange();
        room.selectDraftPosition(new TeamLeaderId(HOST_ID), 1);
        InMemoryRooms rooms = new InMemoryRooms(room);
        rooms.failOnSave(new ObjectOptimisticLockingFailureException(Room.class, room.getId()));
        ClearDraftPosition clearDraftPosition = new ClearDraftPosition(
            rooms,
            new RoomActionAuthorizer(),
            noopRoomSnapshotPublisher()
        );

        assertThatThrownBy(() -> clearDraftPosition.clear(room.getCode(), HOST_ACTION_TOKEN))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_CONCURRENT_MODIFICATION));
    }

    private static void assertRoomError(CoreException ex, RoomErrorType expected) {
        assertThat(ex.getError()).isEqualTo(expected);
    }

    private static RoomSnapshotPublisher noopRoomSnapshotPublisher() {
        return new NoopRoomSnapshotPublisher();
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
                MIN_BID_UNIT,
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
                    null,
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
                    null,
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

    private static Room waitingDraftRoomForPositionChange() {
        Room room =
            Room.createFromTemplate(
                "DRF001",
                new TeamLeaderId(HOST_ID),
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    PICK_BAN_TIME,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
                        new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
                    )
                ),
                CREATED_AT
            );
        room.join(new TeamLeaderId(GUEST_ID), "게스트", GUEST_ACTION_TOKEN);
        return room;
    }

    private static SettleAuction unsupportedSettleAuction() {
        InMemoryRooms rooms = new InMemoryRooms();
        return new SettleAuction(
            new SettleAuctionAttempt(rooms, Clock.fixed(CREATED_AT, ZoneOffset.UTC), noopRoomSnapshotPublisher()),
            rooms,
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
        );
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

    private static AuctionScheduleReader emptyAuctionScheduleReader() {
        return List::of;
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
            throw new UnsupportedOperationException("start and bid should use saveAndFlush");
        }

        @Override
        public Room saveAndFlush(Room room) {
            if (saveFailure != null) {
                throw saveFailure;
            }
            this.room = room;
            markFlushed(room);
            return room;
        }

        @Override
        public Optional<Room> findById(RoomId id) {
            return Optional.ofNullable(room).filter(it -> it.getId().equals(id));
        }

        @Override
        public Optional<Room> findByCode(String code) {
            return Optional.ofNullable(room).filter(it -> it.getCode().equals(code));
        }

        private void markFlushed(Room room) {
            try {
                var field = Room.class.getDeclaredField("version");
                field.setAccessible(true);
                field.setLong(room, 1L);
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            }
        }
    }

    private static final class RecordingRoomSnapshotPublisher implements RoomSnapshotPublisher {
        private final java.util.ArrayList<RoomRealtimeSnapshotEvent> events = new java.util.ArrayList<>();

        @Override
        public void publishAfterCommit(Room room) {
            events.add(RoomRealtimeSnapshotEvent.from(room, CREATED_AT));
        }
    }
}
