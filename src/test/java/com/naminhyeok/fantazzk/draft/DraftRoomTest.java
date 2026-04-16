package com.naminhyeok.fantazzk.draft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DraftRoomTest {
    private static final String HOST_ID = "host-1";
    private static final String GUEST_ID = "guest-1";

    @Test
    void 방을_생성하면_선수가_순서대로_정렬된다() {
        DraftRoom room = DraftRoom.create(
            new DraftRoomId("ROOM01"),
            2,
            2,
            DraftOrderStrategy.SNAKE,
            List.of(
                new DraftPlayerSpec(1, "선수B", "JUNGLE", 1),
                new DraftPlayerSpec(0, "선수A", "TOP", 0)
            )
        );

        assertThat(room.snapshot().players())
            .extracting(DraftRoomState.Player::playerId, DraftRoomState.Player::name, DraftRoomState.Player::position)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, "선수A", "TOP"),
                org.assertj.core.groups.Tuple.tuple(1, "선수B", "JUNGLE")
            );
        assertThat(room.snapshot().readiness()).isEqualTo(DraftRoomReadiness.WAITING_FOR_LEADERS);
    }

    @Test
    void 선수_수는_팀_구성에_맞아야_한다() {
        assertThatThrownBy(() ->
            DraftRoom.create(
                new DraftRoomId("ROOM01"),
                2,
                2,
                DraftOrderStrategy.SNAKE,
                List.of(new DraftPlayerSpec(0, "선수A", "TOP", 0))
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("선수 수는 정확히 2명이어야 합니다");
    }

    @Nested
    class 팀장_배치 {
        @Test
        void 팀장을_추가하면_중복_닉네임을_막는다() {
            DraftRoom room = waitingDraftRoom();
            room.addLeader(HOST_ID, "Faker");

            assertThatThrownBy(() -> room.addLeader(GUEST_ID, "  faker  "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("팀장 닉네임이 이미 사용 중입니다");
        }

        @Test
        void 드래프트_자리를_선택하면_팀장에게_확정된다() {
            DraftRoom room = waitingDraftRoom();
            room.addLeader(HOST_ID, "호스트");
            room.addLeader(GUEST_ID, "게스트");

            room.selectDraftPosition(HOST_ID, 2);

            assertThat(room.snapshot().leaders())
                .extracting(DraftRoomState.Leader::leaderId, DraftRoomState.Leader::draftPosition)
                .containsExactly(
                    org.assertj.core.groups.Tuple.tuple(HOST_ID, 2),
                    org.assertj.core.groups.Tuple.tuple(GUEST_ID, null)
                );
            assertThat(room.snapshot().readiness()).isEqualTo(DraftRoomReadiness.WAITING_FOR_DRAFT_POSITIONS);
        }

        @Test
        void 이미_선점된_드래프트_자리는_선택할_수_없다() {
            DraftRoom room = waitingDraftRoom();
            room.addLeader(HOST_ID, "호스트");
            room.addLeader(GUEST_ID, "게스트");
            room.selectDraftPosition(HOST_ID, 1);

            assertThatThrownBy(() -> room.selectDraftPosition(GUEST_ID, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("드래프트 자리가 이미 사용 중입니다");
        }

        @Test
        void 드래프트_자리를_취소하면_미선택으로_돌아간다() {
            DraftRoom room = waitingDraftRoom();
            room.addLeader(HOST_ID, "호스트");
            room.addLeader(GUEST_ID, "게스트");
            room.selectDraftPosition(HOST_ID, 1);

            room.clearDraftPosition(HOST_ID);

            assertThat(room.snapshot().leaders())
                .extracting(DraftRoomState.Leader::leaderId, DraftRoomState.Leader::draftPosition)
                .containsExactly(
                    org.assertj.core.groups.Tuple.tuple(HOST_ID, null),
                    org.assertj.core.groups.Tuple.tuple(GUEST_ID, null)
                );
        }
    }

    @Nested
    class 시작 {
        @Test
        void 드래프트_방은_자리_확정이_끝나야_시작할_수_있다() {
            DraftRoom room = waitingDraftRoom();
            room.addLeader(HOST_ID, "호스트");
            room.addLeader(GUEST_ID, "게스트");
            room.selectDraftPosition(HOST_ID, 1);

            assertThat(room.snapshot().readiness()).isEqualTo(DraftRoomReadiness.WAITING_FOR_DRAFT_POSITIONS);
            assertThatThrownBy(room::start)
                .isInstanceOf(DraftRoomStateInvalidException.class)
                .hasMessage("드래프트를 시작할 수 없습니다: 드래프트 자리가 아직 확정되지 않았습니다");
        }

        @Test
        void 드래프트_방을_시작하면_현재_턴이_초기화된다() {
            DraftRoom room = startedDraftRoom();

            assertThat(room.snapshot().status()).isEqualTo(DraftRoomStatus.IN_PROGRESS);
            assertThat(room.snapshot().progress()).isNotNull();
            assertThat(room.snapshot().progress().currentTurnIndex()).isEqualTo(0);
        }
    }

    @Nested
    class 진행 {
        @Test
        void 현재_턴의_팀장이_선수를_픽할_수_있다() {
            DraftRoom room = startedDraftRoom();

            DraftMember member = room.pick(HOST_ID, 0);

            assertThat(member.leaderId()).isEqualTo(HOST_ID);
            assertThat(member.playerId()).isEqualTo(0);
            assertThat(room.snapshot().players())
                .extracting(DraftRoomState.Player::assigned)
                .containsExactly(true, false);
            assertThat(room.snapshot().progress().currentTurnIndex()).isEqualTo(1);
        }

        @Test
        void 자신의_턴이_아니면_픽할_수_없다() {
            DraftRoom room = startedDraftRoom();

            assertThatThrownBy(() -> room.pick(GUEST_ID, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 턴이 아닙니다");
        }

        @Test
        void 이미_배정된_선수는_픽할_수_없다() {
            DraftRoom room = startedDraftRoom();
            room.pick(HOST_ID, 0);

            assertThatThrownBy(() -> room.pick(GUEST_ID, 0))
                .isInstanceOf(DraftRoomStateInvalidException.class)
                .hasMessage("선수를 찾을 수 없습니다: 0");
        }

        @Test
        void 존재하지_않는_선수는_픽할_수_없다() {
            DraftRoom room = startedDraftRoom();

            assertThatThrownBy(() -> room.pick(HOST_ID, 99))
                .isInstanceOf(DraftRoomStateInvalidException.class)
                .hasMessage("선수를 찾을 수 없습니다: 99");
        }

        @Test
        void 모든_픽이_완료되면_방이_완료된다() {
            DraftRoom room = startedDraftRoom();

            room.pick(HOST_ID, 0);
            room.pick(GUEST_ID, 1);

            assertThat(room.snapshot().status()).isEqualTo(DraftRoomStatus.COMPLETED);
            assertThat(room.snapshot().readiness()).isEqualTo(DraftRoomReadiness.COMPLETED);
            assertThat(room.snapshot().members()).hasSize(2);
        }

        @Test
        void SNAKE_드래프트는_2라운드에서_역순으로_진행된다() {
            DraftRoom room = startedDraftRoom(DraftOrderStrategy.SNAKE, 3, List.of("선수1", "선수2", "선수3", "선수4"));

            room.pick(HOST_ID, 0);
            room.pick(GUEST_ID, 1);

            assertThatThrownBy(() -> room.pick(HOST_ID, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 턴이 아닙니다");

            DraftMember thirdPick = room.pick(GUEST_ID, 2);

            assertThat(thirdPick.leaderId()).isEqualTo(GUEST_ID);
            assertThat(room.snapshot().progress().currentTurnIndex()).isEqualTo(3);
            assertThat(room.snapshot().progress().currentRoundLeaderIds()).containsExactly(GUEST_ID, HOST_ID);
        }

        @Test
        void FIXED_드래프트는_2라운드에서도_같은_순서로_진행된다() {
            DraftRoom room = startedDraftRoom(DraftOrderStrategy.FIXED, 3, List.of("선수1", "선수2", "선수3", "선수4"));

            room.pick(HOST_ID, 0);
            room.pick(GUEST_ID, 1);

            assertThatThrownBy(() -> room.pick(GUEST_ID, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("현재 턴이 아닙니다");

            DraftMember thirdPick = room.pick(HOST_ID, 2);

            assertThat(thirdPick.leaderId()).isEqualTo(HOST_ID);
            assertThat(room.snapshot().progress().currentTurnIndex()).isEqualTo(3);
            assertThat(room.snapshot().progress().currentRoundLeaderIds()).containsExactly(HOST_ID, GUEST_ID);
        }
    }

    private static DraftRoom waitingDraftRoom() {
        return DraftRoom.create(
            new DraftRoomId("ROOM01"),
            2,
            2,
            DraftOrderStrategy.SNAKE,
            List.of(
                new DraftPlayerSpec(0, "선수1", "TOP", 0),
                new DraftPlayerSpec(1, "선수2", "JUNGLE", 1)
            )
        );
    }

    private static DraftRoom startedDraftRoom() {
        DraftRoom room = waitingDraftRoom();
        room.addLeader(HOST_ID, "호스트");
        room.addLeader(GUEST_ID, "게스트");
        room.selectDraftPosition(HOST_ID, 1);
        room.selectDraftPosition(GUEST_ID, 2);
        room.start();
        return room;
    }

    private static DraftRoom startedDraftRoom(
        DraftOrderStrategy draftOrderStrategy,
        int teamSize,
        List<String> playerNames
    ) {
        DraftRoom room =
            DraftRoom.create(
                new DraftRoomId("ROOM02"),
                2,
                teamSize,
                draftOrderStrategy,
                List.of(
                    new DraftPlayerSpec(0, playerNames.getFirst(), "TOP", 0),
                    new DraftPlayerSpec(1, playerNames.get(1), "JUNGLE", 1),
                    new DraftPlayerSpec(2, playerNames.get(2), "MID", 2),
                    new DraftPlayerSpec(3, playerNames.get(3), "ADC", 3)
                )
            );
        room.addLeader(HOST_ID, "호스트");
        room.addLeader(GUEST_ID, "게스트");
        room.selectDraftPosition(HOST_ID, 1);
        room.selectDraftPosition(GUEST_ID, 2);
        room.start();
        return room;
    }
}
