package com.naminhyeok.fantazzk.auction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuctionRoomTest {
    @Test
    void 경매_방을_시작하면_1라운드_deadline이_생성된다() {
        AuctionRoom room = AuctionTestFixtures.waitingRoomWithGuestLeader();

        room.start(AuctionTestFixtures.HOST_ID, AuctionTestFixtures.CREATED_AT);

        AuctionRoomState state = room.readState();
        assertThat(state.currentRound()).isEqualTo(1);
        assertThat(state.currentRoundEndsAt()).isEqualTo(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME));
    }

    @Test
    void deadline_전에는_경매를_정산할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        assertThatThrownBy(() -> room.settle(AuctionTestFixtures.CREATED_AT.plusSeconds(10)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("경매 라운드가 아직 종료되지 않았습니다");
    }

    @Test
    void deadline_가_지나면_입찰할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("경매 라운드가 아직 종료되지 않았습니다");
    }

    @Test
    void 현재_최고가보다_낮거나_같은_입찰은_할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));

        assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.GUEST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(2)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("현재 최고가보다 낮거나 같은 입찰입니다");
    }

    @Test
    void 첫_입찰은_최소_입찰_단위_이상이어야_한다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.HOST_ID, 5, AuctionTestFixtures.CREATED_AT.plusSeconds(1)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("최소 입찰 단위를 만족하지 않습니다");
    }

    @Test
    void 이후_입찰은_현재_최고가보다_최소_입찰_단위만큼_높아야_한다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));

        assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.GUEST_ID, 105, AuctionTestFixtures.CREATED_AT.plusSeconds(2)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("최소 입찰 단위를 만족하지 않습니다");
    }

    @Test
    void 최소_입찰_단위를_만족하면_다음_입찰을_할_수_있다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));
        AuctionBid bid = room.placeBid(AuctionTestFixtures.GUEST_ID, 110, AuctionTestFixtures.CREATED_AT.plusSeconds(2));

        assertThat(bid.amount()).isEqualTo(110);
        assertThat(bid.sequence()).isEqualTo(2);
        assertThat(room.readState().currentRoundEndsAt()).isEqualTo(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 2L));
    }

    @Test
    void 진행_중이_아니면_입찰할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.waitingRoom();

        assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("진행 중인 경매가 아닙니다");
    }

    @Test
    void 팀장_닉네임이_중복되면_추가할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.waitingRoom();

        assertThatThrownBy(() -> room.addLeader("guest-2", "호스트"))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("이미 사용 중인 닉네임입니다: 호스트");
    }

    @Test
    void 예산이_부족하면_입찰할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.HOST_ID, 400, AuctionTestFixtures.CREATED_AT.plusSeconds(1)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("예산이 부족합니다");
    }

    @Test
    void 입찰_금액은_0보다_커야_한다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        for (int amount : List.of(0, -1)) {
            assertThatThrownBy(() -> room.placeBid(AuctionTestFixtures.HOST_ID, amount, AuctionTestFixtures.CREATED_AT.plusSeconds(1)))
                .isInstanceOf(AuctionRoomException.class)
                .hasMessage("입찰 금액은 0보다 커야 합니다");
        }
    }

    @Test
    void 방에_없는_팀장_id로는_입찰할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        assertThatThrownBy(() -> room.placeBid("unknown-leader", 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1)))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("입찰자를 찾을 수 없습니다: unknown-leader");
    }

    @Test
    void 낙찰되면_선수가_배정되고_예산이_차감되며_다음_라운드로_진행한다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        AuctionBid firstBid = room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));
        AuctionBid secondBid = room.placeBid(AuctionTestFixtures.GUEST_ID, 150, AuctionTestFixtures.CREATED_AT.plusSeconds(2));

        AuctionSettlement settlement = room.settle(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 2L));
        AuctionRoomState state = room.readState();

        assertThat(firstBid.sequence()).isEqualTo(1);
        assertThat(secondBid.sequence()).isEqualTo(2);
        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.SOLD);
        assertThat(settlement.playerId()).isEqualTo(0);
        assertThat(settlement.playerName()).isEqualTo("선수1");
        assertThat(state.currentRound()).isEqualTo(2);
        assertThat(state.currentRoundEndsAt()).isEqualTo(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME * 2L + 2L));
        assertThat(state.currentTarget().playerId()).isEqualTo(1);
    }

    @Test
    void 새_경매_라운드가_시작되면_입찰_순번은_다시_처음부터_시작한다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));
        room.settle(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 1L));

        AuctionBid nextRoundBid = room.placeBid(AuctionTestFixtures.HOST_ID, 120, AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 1L));

        assertThat(nextRoundBid.sequence()).isEqualTo(1);
        assertThat(nextRoundBid.round()).isEqualTo(2);
    }

    @Test
    void 모든_선수를_배정하면_deadline이_사라진다() {
        AuctionRoom room = AuctionTestFixtures.waitingRoomWithGuestLeader();
        room.start(AuctionTestFixtures.HOST_ID, AuctionTestFixtures.CREATED_AT);

        room.placeBid(AuctionTestFixtures.HOST_ID, 100, AuctionTestFixtures.CREATED_AT.plusSeconds(1));
        room.placeBid(AuctionTestFixtures.GUEST_ID, 150, AuctionTestFixtures.CREATED_AT.plusSeconds(2));
        room.settle(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 2L));

        room.placeBid(AuctionTestFixtures.HOST_ID, 120, AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 1L));
        room.placeBid(AuctionTestFixtures.GUEST_ID, 130, AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME + 2L));
        room.settle(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME * 2L + 2L));

        assertThat(room.readState().status()).isEqualTo(AuctionRoomStatus.COMPLETED);
        assertThat(room.readState().currentRoundEndsAt()).isNull();
    }

    @Test
    void 진행_중이_아니면_낙찰_처리를_할_수_없다() {
        AuctionRoom room = AuctionTestFixtures.waitingRoom();

        assertThatThrownBy(() -> room.settle(AuctionTestFixtures.CREATED_AT))
            .isInstanceOf(AuctionRoomException.class)
            .hasMessage("진행 중인 경매가 아닙니다");
    }

    @Test
    void 시작되지_않은_경매는_라운드가_없다() {
        AuctionRoom room = AuctionTestFixtures.waitingRoom();

        assertThat(room.readState().currentRound()).isNull();
        assertThat(room.readState().currentTarget()).isNull();
    }

    @Test
    void 입찰_없으면_정산시_패스하고_선수를_뒤로_보낸다() {
        AuctionRoom room = AuctionTestFixtures.startedRoom();

        AuctionSettlement settlement = room.settle(AuctionTestFixtures.CREATED_AT.plusSeconds(AuctionTestFixtures.PICK_BAN_TIME));

        assertThat(settlement.outcome()).isEqualTo(AuctionOutcome.PASSED);
        assertThat(settlement.playerId()).isEqualTo(0);
        assertThat(room.readState().currentRound()).isEqualTo(2);
        assertThat(room.readState().currentTarget().playerId()).isEqualTo(1);
    }
}
