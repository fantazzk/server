package com.naminhyeok.fantazzk.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomDraftTest {
    @Test
    void 현재_턴의_팀장이_선수를_픽할_수_있다() {
        Room room = startedDraftRoom();
        String currentLeaderId = room.getLeaders().getFirst().getTeamLeaderId();

        RoomTeamMember member = room.pick(currentLeaderId, "선수1");

        assertThat(member.getTeamLeaderId()).isEqualTo(currentLeaderId);
        assertThat(member.getPlayerName()).isEqualTo("선수1");
        assertThat(room.getPlayers().getFirst().getStatus()).isEqualTo(PlayerStatus.ASSIGNED);
        assertThat(room.getCurrentTurnIndex()).isEqualTo(1);
    }

    @Test
    void 자신의_턴이_아니면_픽할_수_없다() {
        Room room = startedDraftRoom();
        String otherLeaderId = room.getLeaders().getLast().getTeamLeaderId();

        assertThatThrownBy(() -> room.pick(otherLeaderId, "선수1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("현재 턴이 아닙니다");
    }

    @Test
    void 모든_픽이_완료되면_방이_완료된다() {
        Room room = startedDraftRoom();
        String firstLeaderId = room.getLeaders().getFirst().getTeamLeaderId();
        String secondLeaderId = room.getLeaders().getLast().getTeamLeaderId();

        room.pick(firstLeaderId, "선수1");
        room.pick(secondLeaderId, "선수2");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.COMPLETED);
        assertThat(room.getMembers()).hasSize(2);
    }

    private static Room startedDraftRoom() {
        Room room =
            Room.createFromTemplate(
                "DRF001",
                UUID.randomUUID().toString(),
                "호스트",
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
        room.join("guest-1", "게스트");
        room.start();
        return room;
    }
}
