package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomAuctionTest {
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_ACTION_TOKEN = "guest-action-token";

    @Test
    void 현재_최고가보다_낮거나_같은_입찰은_할_수_없다() {
        Room room = startedAuctionRoom();
        String hostLeaderId = room.getLeaders().getFirst().getTeamLeaderId();
        String guestLeaderId = room.getLeaders().getLast().getTeamLeaderId();

        room.placeBid(hostLeaderId, 100);

        assertThatThrownBy(() -> room.placeBid(guestLeaderId, 100))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_TOO_LOW));
    }

    @Test
    void 진행_중이_아니면_입찰할_수_없다() {
        Room room = waitingAuctionRoom();

        assertThatThrownBy(() -> room.placeBid(HOST_ID, 100))
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS));
    }

    @Test
    void 드래프트_방에서는_입찰할_수_없다() {
        Room room = startedDraftRoomForAuctionError();

        assertThatThrownBy(() -> room.placeBid(HOST_ID, 100))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE));
    }

    @Test
    void 예산이_부족하면_입찰할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.placeBid(HOST_ID, 400))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_BUDGET_EXCEEDED));
    }

    @Test
    void 입찰_금액은_0보다_커야_한다() {
        Room room = startedAuctionRoom();
        String hostLeaderId = room.getLeaders().getFirst().getTeamLeaderId();

        for (int amount : List.of(0, -1)) {
            assertThatThrownBy(() -> room.placeBid(hostLeaderId, amount))
                .isInstanceOf(CoreException.class)
                .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_AMOUNT_NOT_POSITIVE));
        }
    }

    @Test
    void 방에_없는_팀장_id로는_입찰할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.placeBid("unknown-leader", 100))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BIDDER_NOT_FOUND));
    }

    @Test
    void 낙찰되면_선수가_배정되고_예산이_차감되며_다음_라운드로_진행한다() {
        Room room = startedAuctionRoom();
        String hostLeaderId = room.getLeaders().getFirst().getTeamLeaderId();
        String guestLeaderId = room.getLeaders().getLast().getTeamLeaderId();

        room.placeBid(hostLeaderId, 100);
        room.placeBid(guestLeaderId, 150);

        AuctionSettlement settlement = room.settleAuction();

        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(room.getMembers()).singleElement()
            .extracting(RoomTeamMember::getTeamLeaderId, RoomTeamMember::getPlayerName)
            .containsExactly(guestLeaderId, "선수1");
        assertThat(room.getLeaders().stream().filter(it -> it.getTeamLeaderId().equals(guestLeaderId)).findFirst().orElseThrow().getRemainingBudget())
            .isEqualTo(150);
        assertThat(room.getPlayers().getFirst().getStatus()).isEqualTo(PlayerStatus.ASSIGNED);
        assertThat(room.getCurrentAuctionRound()).isEqualTo(2);
    }

    @Test
    void 진행_중이_아니면_낙찰_처리를_할_수_없다() {
        Room room = waitingAuctionRoom();

        assertThatThrownBy(room::settleAuction)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS));
    }

    @Test
    void 드래프트_방에서는_낙찰_처리를_할_수_없다() {
        Room room = startedDraftRoomForAuctionError();

        assertThatThrownBy(room::settleAuction)
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE));
    }

    private static void assertRoomError(CoreException ex, RoomErrorType expected) {
        assertThat(ex.getError()).isEqualTo(expected);
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "ROOM01",
            HOST_ID,
            "호스트",
            HOST_ACTION_TOKEN,
            new RoomTemplateSpec(
                RoomTemplateSpec.Mode.AUCTION,
                2,
                2,
                300,
                null,
                List.of(
                    new RoomTemplateSpec.Player("선수1", 0),
                    new RoomTemplateSpec.Player("선수2", 1)
                )
            )
        );
    }

    private static Room startedDraftRoomForAuctionError() {
        Room room =
            Room.createFromTemplate(
                "ROOM02",
                HOST_ID,
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    2,
                    null,
                    RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    List.of(
                        new RoomTemplateSpec.Player("선수1", 0),
                        new RoomTemplateSpec.Player("선수2", 1)
                    )
                )
            );
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);
        room.selectDraftPosition(GUEST_ID, 2);
        room.start(HOST_ID);
        return room;
    }

    private static Room startedAuctionRoom() {
        Room room = waitingAuctionRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.start(HOST_ID);
        return room;
    }
}
