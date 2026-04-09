package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RoomDraftTest {
    private static final Instant CREATED_AT = Instant.parse("2026-04-09T00:00:00Z");
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
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PICK_OUT_OF_TURN));
    }

    @Test
    void 진행_중이_아니면_픽할_수_없다() {
        Room room = waitingDraftRoom();

        assertThatThrownBy(() -> room.pick(HOST_ID, "선수1"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS));
    }

    @Test
    void 경매_방에서는_픽할_수_없다() {
        Room room = startedAuctionRoomForDraftError();

        assertThatThrownBy(() -> room.pick(HOST_ID, "선수1"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PICK_REQUIRES_DRAFT_MODE));
    }

    @Test
    void 이미_배정된_선수는_픽할_수_없다() {
        Room room = startedDraftRoom();

        room.pick(HOST_ID, "선수1");

        assertThatThrownBy(() -> room.pick(GUEST_ID, "선수1"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PICK_PLAYER_NOT_AVAILABLE));
    }

    @Test
    void 존재하지_않는_선수는_픽할_수_없다() {
        Room room = startedDraftRoom();

        assertThatThrownBy(() -> room.pick(HOST_ID, "없는 선수"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PICK_PLAYER_NOT_AVAILABLE));
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

    void SNAKE_드래프트는_2라운드에서_역순으로_진행된다() {
        Room room = startedDraftRoom(RoomTemplateSpec.DraftOrderStrategy.SNAKE, 3, List.of("선수1", "선수2", "선수3", "선수4"));

        room.pick(HOST_ID, "선수1");
        room.pick(GUEST_ID, "선수2");

        assertThatThrownBy(() -> room.pick(HOST_ID, "선수3"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PICK_OUT_OF_TURN));

        RoomTeamMember thirdPick = room.pick(GUEST_ID, "선수3");

        assertThat(thirdPick.getTeamLeaderId()).isEqualTo(GUEST_ID);
        assertThat(room.getCurrentTurnIndex()).isEqualTo(3);
    }

    @Test
    void FIXED_드래프트는_2라운드에서도_같은_순서로_진행된다() {
        Room room = startedDraftRoom(RoomTemplateSpec.DraftOrderStrategy.FIXED, 3, List.of("선수1", "선수2", "선수3", "선수4"));

        room.pick(HOST_ID, "선수1");
        room.pick(GUEST_ID, "선수2");

        assertThatThrownBy(() -> room.pick(GUEST_ID, "선수3"))
            .isInstanceOf(CoreException.class)
            .isInstanceOfSatisfying(CoreException.class, ex -> assertRoomError(ex, RoomErrorType.ROOM_PICK_OUT_OF_TURN));

        RoomTeamMember thirdPick = room.pick(HOST_ID, "선수3");

        assertThat(thirdPick.getTeamLeaderId()).isEqualTo(HOST_ID);
        assertThat(room.getCurrentTurnIndex()).isEqualTo(3);
    }

    private static void assertRoomError(CoreException ex, RoomErrorType expected) {
        assertThat(ex.getError()).isEqualTo(expected);
    }

    private static Room startedDraftRoom() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);
        room.selectDraftPosition(GUEST_ID, 2);
        room.start(HOST_ID);
        return room;
    }

    private static Room startedAuctionRoomForDraftError() {
        Room room = waitingAuctionRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.start(HOST_ID);
        return room;
    }

    private static Room startedDraftRoom(
        RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy,
        int teamSize,
        List<String> playerNames
    ) {
        Room room = waitingDraftRoom(draftOrderStrategy, teamSize, playerNames);
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);
        room.selectDraftPosition(GUEST_ID, 2);
        room.start(HOST_ID);
        return room;
    }

    private static Room waitingDraftRoom() {
        return waitingDraftRoom(
            RoomTemplateSpec.DraftOrderStrategy.SNAKE,
            2,
            List.of("선수1", "선수2")
        );
    }

    private static Room waitingDraftRoom(
        RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy,
        int teamSize,
        List<String> playerNames
    ) {
        Room room =
            Room.createFromTemplate(
                "DRF001",
                HOST_ID,
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.DRAFT,
                    2,
                    teamSize,
                    null,
                    draftOrderStrategy,
                    IntStream.range(0, playerNames.size())
                        .mapToObj(index -> new RoomTemplateSpec.Player(playerNames.get(index), index))
                        .toList()
                ),
                CREATED_AT
            );
        return room;
    }

    private static Room waitingAuctionRoom() {
        return Room.createFromTemplate(
            "AUC001",
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
            ),
            CREATED_AT
        );
    }
}
