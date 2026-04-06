package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.domain.DraftBoard;
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.DraftPickSettlement;
import java.util.List;
import org.junit.jupiter.api.Test;

class DraftBoardTest {

    @Test
    void snake_strategy_reverses_pick_order_on_every_other_round() {
        DraftBoard board = new DraftBoard(List.of("A", "B", "C"), DraftOrderStrategy.SNAKE, 2);

        assertThat(board.pickOrder()).containsExactly("A", "B", "C", "C", "B", "A");
        assertThat(board.currentTeamLeader(4)).isEqualTo("B");
    }

    @Test
    void turn_ownership_is_validated_and_negative_turns_are_rejected() {
        DraftBoard board = new DraftBoard(List.of("A", "B"), DraftOrderStrategy.FIXED, 2);

        assertThatThrownBy(() -> board.currentTeamLeader(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("드래프트 턴은 0 이상이어야 합니다");
        assertThatThrownBy(() -> board.requireTurnOwner(1, "A"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("현재 턴이 아닙니다");
        assertThatCode(() -> board.requireTurnOwner(1, "B")).doesNotThrowAnyException();
    }

    @Test
    void pick_settlement_reports_next_turn_and_completion() {
        DraftBoard board = new DraftBoard(List.of("A", "B"), DraftOrderStrategy.SNAKE, 2);

        DraftPickSettlement settlement = board.settlePick(2, 4);

        assertThat(settlement.getNextTurnIndex()).isEqualTo(3);
        assertThat(settlement.isCompleted()).isTrue();
    }
}
