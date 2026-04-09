package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class RoomDraftTest {
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_ACTION_TOKEN = "guest-action-token";

    @Test
    void 현재_턴의_팀장이_선수를_픽할_수_있다() {
        Room room = startedDraftRoom();
        String currentLeaderId = HOST_ID;

        RoomTeamMember member = room.pick(currentLeaderId, "선수1");

        assertThat(member.getTeamLeaderId()).isEqualTo(currentLeaderId);
        assertThat(member.getPlayerName()).isEqualTo("선수1");
        assertThat(room.getPlayers().getFirst().getStatus()).isEqualTo(PlayerStatus.ASSIGNED);
        assertThat(room.getCurrentTurnIndex()).isEqualTo(1);
    }

    @Test
    void 자신의_턴이_아니면_픽할_수_없다() {
        Room room = startedDraftRoom();
        String otherLeaderId = GUEST_ID;

        assertThatThrownBy(() -> room.pick(otherLeaderId, "선수1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("현재 턴이 아닙니다");
    }

    @Test
    void 모든_픽이_완료되면_방이_완료된다() {
        Room room = startedDraftRoom();
        String firstLeaderId = HOST_ID;
        String secondLeaderId = GUEST_ID;

        room.pick(firstLeaderId, "선수1");
        room.pick(secondLeaderId, "선수2");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.COMPLETED);
        assertThat(room.getMembers()).hasSize(2);
    }

    @Test
    void 확정된_드래프트_자리_순서대로_첫_턴이_정해진다() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 2);
        room.selectDraftPosition(GUEST_ID, 1);
        room.start(HOST_ID);

        RoomTeamMember member = room.pick(GUEST_ID, "선수1");

        assertThat(member.getTeamLeaderId()).isEqualTo(GUEST_ID);
        assertThat(room.getCurrentTurnIndex()).isEqualTo(1);
    }

    private static Room startedDraftRoom() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);
        room.selectDraftPosition(GUEST_ID, 2);
        room.start(HOST_ID);
        return room;
    }

    private static Room waitingDraftRoom() {
        Room room =
            Room.createFromTemplate(
                "DRF001",
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
        return room;
    }
}
