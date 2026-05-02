package com.naminhyeok.fantazzk.room.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class RoomTemplateSpecTest {
    @Test
    void 드래프트_명세에는_순서_전략이_필요하다() {
        assertThatThrownBy(
            () -> new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
                RoomMode.DRAFT,
                2,
                2,
                null,
                30,
                null,
                null,
                players()
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("드래프트 방 생성 명세에는 순서 전략이 필요합니다");
    }

    @Test
    void 경매_명세에는_예산이_필요하다() {
        assertThatThrownBy(
            () -> new RoomTemplateSpec(
                "LEAGUE_OF_LEGENDS",
                RoomMode.AUCTION,
                2,
                2,
                null,
                30,
                10,
                null,
                players()
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("경매 방 생성 명세에는 예산이 필요합니다");
    }

    private List<RoomTemplateSpec.Player> players() {
        return List.of(
            new RoomTemplateSpec.Player(new RoomPlayerId(0), "선수1", "TOP", 0),
            new RoomTemplateSpec.Player(new RoomPlayerId(1), "선수2", "JUNGLE", 1)
        );
    }
}
