package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomAuctionTest {
    @Test
    void 현재_최고가보다_낮거나_같은_입찰은_할_수_없다() {
        Room room = startedAuctionRoom();
        String hostLeaderId = room.getLeaders().getFirst().getTeamLeaderId();
        String guestLeaderId = room.getLeaders().getLast().getTeamLeaderId();

        room.placeBid(hostLeaderId, 100);

        assertThatThrownBy(() -> room.placeBid(guestLeaderId, 100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("현재 최고가보다 높아야 합니다");
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

    private static Room startedAuctionRoom() {
        Room room =
            Room.createFromTemplate(
                "AUC001",
                UUID.randomUUID().toString(),
                "호스트",
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
        room.join("guest-1", "게스트");
        room.start();
        return room;
    }
}
