package com.naminhyeok.fantazzk.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.CoreException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RoomAggregateTest {
    @Test
    void 템플릿_명세로_방을_생성하면_선수와_호스트를_초기화한다() {
        Room room =
            Room.createFromTemplate(
                "ROOM01",
                UUID.randomUUID().toString(),
                "호스트",
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
    }

    @Test
    void 참가하면_팀장을_추가한다() {
        Room room = auctionWaitingRoom();

        room.join("guest-1", "게스트");

        assertThat(room.getLeaders()).hasSize(2);
        assertThat(room.getLeaders().getLast().getNickname()).isEqualTo("게스트");
    }

    @Test
    void 대기_상태가_아니면_참가할_수_없다() {
        Room room = startedAuctionRoom();

        assertThatThrownBy(() -> room.join("guest-2", "추가 게스트"))
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
                UUID.randomUUID().toString(),
                "호스트",
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

        assertThatThrownBy(() -> room.join("guest-1", "게스트"))
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
            room.join("guest-1", "게스트");

            room.start();

            assertThat(room.getStatus()).isEqualTo(RoomStatus.IN_PROGRESS);
            assertThat(room.getCurrentAuctionRound()).isEqualTo(1);
            assertThat(room.getCurrentTurnIndex()).isNull();
        }

        @Test
        void 드래프트_방을_시작하면_현재_턴을_초기화한다() {
            Room room =
                Room.createFromTemplate(
                    "ROOM02",
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

            assertThat(room.getStatus()).isEqualTo(RoomStatus.IN_PROGRESS);
            assertThat(room.getCurrentTurnIndex()).isEqualTo(0);
            assertThat(room.getCurrentAuctionRound()).isNull();
        }

        @Test
        void 팀장_자리가_다_차지_않으면_시작할_수_없다() {
            Room room = auctionWaitingRoom();

            assertThatThrownBy(room::start)
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreException = (CoreException) ex;
                    assertThat(coreException.getError()).isEqualTo(RoomErrorType.ROOM_LEADERS_NOT_FULL);
                    assertThat(coreException.getData()).isNull();
                });
        }
    }

    private static Room auctionWaitingRoom() {
        return Room.createFromTemplate(
            "ROOM01",
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
    }

    private static Room startedAuctionRoom() {
        Room room = auctionWaitingRoom();
        room.join("guest-1", "게스트");
        room.start();
        return room;
    }
}
