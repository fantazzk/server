package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RoomAggregateTest {
    private static final String HOST_ID = "host-1";
    private static final String HOST_ACTION_TOKEN = "host-action-token";
    private static final String GUEST_ID = "guest-1";
    private static final String GUEST_ACTION_TOKEN = "guest-action-token";

    @Test
    void 템플릿_명세로_방을_생성하면_선수와_호스트를_초기화한다() {
        Room room =
            Room.createFromTemplate(
                "ROOM01",
                HOST_ID,
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
                    2,
                    3,
                    300,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player("선수B", 1),
                        new RoomTemplateSpec.Player("선수A", 0)
                    )
                )
            );

        assertThat(room.getCode()).isEqualTo("ROOM01");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(room.getMode()).isEqualTo(RoomMode.AUCTION);
        assertThat(room.getPlayers().stream().map(RoomPlayer::getName)).containsExactly("선수A", "선수B");

        RoomTeamLeader hostLeader = room.getLeaders().getFirst();
        assertThat(hostLeader.getNickname()).isEqualTo("호스트");
        assertThat(hostLeader.getRemainingBudget()).isEqualTo(300);
        assertThat(hostLeader.getActionToken()).isEqualTo(HOST_ACTION_TOKEN);
    }

    @Test
    void 참가하면_팀장을_추가한다() {
        Room room = auctionWaitingRoom();

        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);

        assertThat(room.getLeaders()).hasSize(2);
        assertThat(room.getLeaders().getLast().getNickname()).isEqualTo("게스트");
        assertThat(room.getLeaders().getLast().getActionToken()).isEqualTo(GUEST_ACTION_TOKEN);
    }

    @Test
    void 드래프트_자리를_선택하면_팀장에게_확정된다() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);

        room.selectDraftPosition(HOST_ID, 2);

        assertThat(room.getLeaders().getFirst().getDraftPosition()).isEqualTo(2);
        assertThat(room.getStartReadiness()).isEqualTo(RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS);
    }

    @Test
    void 드래프트_자리를_다른_빈_자리로_변경할_수_있다() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);

        room.selectDraftPosition(HOST_ID, 2);

        assertThat(room.getLeaders().getFirst().getDraftPosition()).isEqualTo(2);
    }

    @Test
    void 드래프트_자리를_취소하면_미선택으로_돌아간다() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);

        room.clearDraftPosition(HOST_ID);

        assertThat(room.getLeaders().getFirst().getDraftPosition()).isNull();
    }

    @Test
    void 이미_선점된_드래프트_자리는_선택할_수_없다() {
        Room room = waitingDraftRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.selectDraftPosition(HOST_ID, 1);

        assertThatThrownBy(() -> room.selectDraftPosition(GUEST_ID, 1))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_DRAFT_POSITION_TAKEN);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 대기_상태가_아니면_참가할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.join("guest-2", "추가 게스트", "guest-2-action-token"))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_JOIN_REQUIRES_WAITING);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Test
    void 방이_가득_차면_참가할_수_없다() {
        Room room =
            Room.createFromTemplate(
                "ROOM03",
                HOST_ID,
                "호스트",
                HOST_ACTION_TOKEN,
                new RoomTemplateSpec(
                    RoomTemplateSpec.Mode.AUCTION,
                    1,
                    2,
                    300,
                    null,
                    List.of(
                        new RoomTemplateSpec.Player("선수1", 0),
                        new RoomTemplateSpec.Player("선수2", 1)
                    )
                )
        );

        assertThatThrownBy(() -> room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN))
            .isInstanceOf(CoreException.class)
            .satisfies(ex -> {
                CoreException coreException = (CoreException) ex;
                assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_FULL);
                assertThat(coreException.getData()).isNull();
            });
    }

    @Nested
    class 시작 {
        @Test
        void 경매_방을_시작하면_경매_라운드를_초기화한다() {
            Room room = auctionWaitingRoom();
            room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);

            room.start(HOST_ID);

            assertThat(room.getStatus()).isEqualTo(RoomStatus.IN_PROGRESS);
            assertThat(room.getCurrentAuctionRound()).isEqualTo(1);
            assertThat(room.getCurrentTurnIndex()).isNull();
        }

        @Test
        void 드래프트_방을_시작하면_현재_턴을_초기화한다() {
            Room room = waitingDraftRoom();
            room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
            room.selectDraftPosition(HOST_ID, 1);
            room.selectDraftPosition(GUEST_ID, 2);

            room.start(HOST_ID);

            assertThat(room.getStatus()).isEqualTo(RoomStatus.IN_PROGRESS);
            assertThat(room.getCurrentTurnIndex()).isEqualTo(0);
            assertThat(room.getCurrentAuctionRound()).isNull();
        }

        @Test
        void 드래프트_방은_자리_확정이_끝나야_시작할_수_있다() {
            Room room = waitingDraftRoom();
            room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
            room.selectDraftPosition(HOST_ID, 1);

            assertThat(room.getStartReadiness()).isEqualTo(RoomStartReadiness.WAITING_FOR_DRAFT_POSITIONS);
            assertThatThrownBy(() -> room.start(HOST_ID))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_DRAFT_POSITIONS_NOT_FULL);
                    assertThat(coreException.getData()).isNull();
                });
        }

        @Test
        void 팀장_자리가_다_차지_않으면_시작할_수_없다() {
            Room room = auctionWaitingRoom();

            assertThatThrownBy(() -> room.start(HOST_ID))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_LEADERS_NOT_FULL);
                    assertThat(coreException.getData()).isNull();
                });
        }

        @Test
        void 호스트가_아니면_시작할_수_없다() {
            Room room = auctionWaitingRoom();
            room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);

            assertThatThrownBy(() -> room.start(GUEST_ID))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_START_FORBIDDEN);
                    assertThat(coreException.getData()).isNull();
                });
        }
    }

    private static Room auctionWaitingRoom() {
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

    private static Room startedAuctionRoom() {
        Room room = auctionWaitingRoom();
        room.join(GUEST_ID, "게스트", GUEST_ACTION_TOKEN);
        room.start(HOST_ID);
        return room;
    }

    private static Room waitingDraftRoom() {
        return Room.createFromTemplate(
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
    }
}
