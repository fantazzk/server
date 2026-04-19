package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.DraftProgress;
import java.util.List;
import org.junit.jupiter.api.Test;

class DraftProgressTest {
    @Test
    void fixed전략은_라운드가_바뀌어도_같은_순서를_유지한다() {
        DraftProgress progress =
            DraftProgress.from(List.of("host-1", "guest-1"), DraftOrderStrategy.FIXED, 2);

        assertThat(progress.currentTurnIndex()).isEqualTo(2);
        assertThat(progress.currentRound()).isEqualTo(2);
        assertThat(progress.currentLeaderId()).isEqualTo("host-1");
        assertThat(progress.currentRoundLeaderIds()).containsExactly("host-1", "guest-1");
    }

    @Test
    void snake전략은_짝수_라운드에서_역순을_사용한다() {
        DraftProgress progress =
            DraftProgress.from(List.of("host-1", "guest-1"), DraftOrderStrategy.SNAKE, 2);

        assertThat(progress.currentTurnIndex()).isEqualTo(2);
        assertThat(progress.currentRound()).isEqualTo(2);
        assertThat(progress.currentLeaderId()).isEqualTo("guest-1");
        assertThat(progress.currentRoundLeaderIds()).containsExactly("guest-1", "host-1");
    }

    @Test
    void 빈_리더_목록은_거부한다() {
        assertThatThrownBy(() -> DraftProgress.from(List.of(), DraftOrderStrategy.FIXED, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
