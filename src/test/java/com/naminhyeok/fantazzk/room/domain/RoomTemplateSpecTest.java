package com.naminhyeok.fantazzk.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomPlayerId;
import com.naminhyeok.fantazzk.room.domain.RoomTemplateSpec;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomTemplateSpecTest {
    @Test
    void 템플릿_계약을_room_생성_명세로_한_번만_정제한다() {
        RoomTemplateSpec spec =
            RoomTemplateSpec.from(
                new TemplateCatalog.TemplateBlueprint(
                    "LEAGUE_OF_LEGENDS",
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    null,
                    TemplateCatalog.DraftOrderStrategy.SNAKE,
                    List.of(
                        new TemplateCatalog.PlayerBlueprint("선수1", "TOP", 0),
                        new TemplateCatalog.PlayerBlueprint("선수2", "JUNGLE", 1),
                        new TemplateCatalog.PlayerBlueprint("선수3", "MID", 2),
                        new TemplateCatalog.PlayerBlueprint("선수4", "ADC", 3)
                    )
                )
            );

        assertThat(spec.gameType()).isEqualTo("LEAGUE_OF_LEGENDS");
        assertThat(spec.mode()).isEqualTo(RoomMode.DRAFT);
        assertThat(spec.teamCount()).isEqualTo(2);
        assertThat(spec.teamSize()).isEqualTo(3);
        assertThat(spec.pickBanTime()).isEqualTo(30);
        assertThat(spec.draftOrderStrategy()).isEqualTo(DraftOrderStrategy.SNAKE);
        assertThat(spec.players())
            .extracting(RoomTemplateSpec.Player::id, RoomTemplateSpec.Player::name, RoomTemplateSpec.Player::position, RoomTemplateSpec.Player::displayOrder)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(0), "선수1", "TOP", 0),
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(1), "선수2", "JUNGLE", 1),
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(2), "선수3", "MID", 2),
                org.assertj.core.groups.Tuple.tuple(new RoomPlayerId(3), "선수4", "ADC", 3)
            );
    }

    @Test
    void 드래프트_계약에_순서_전략이_없으면_변환을_거부한다() {
        assertThatThrownBy(
            () -> RoomTemplateSpec.from(
                new TemplateCatalog.TemplateBlueprint(
                    "LEAGUE_OF_LEGENDS",
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    2,
                    null,
                    30,
                    null,
                    null,
                    List.of(
                        new TemplateCatalog.PlayerBlueprint("선수1", "TOP", 0),
                        new TemplateCatalog.PlayerBlueprint("선수2", "JUNGLE", 1)
                    )
                )
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("드래프트 방 생성 명세에는 순서 전략이 필요합니다");
    }

    @Test
    void 픽밴_시간이_없으면_변환을_거부한다() {
        assertThatThrownBy(
            () -> RoomTemplateSpec.from(
                new TemplateCatalog.TemplateBlueprint(
                    "LEAGUE_OF_LEGENDS",
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    2,
                    300,
                    null,
                    10,
                    null,
                    List.of(
                        new TemplateCatalog.PlayerBlueprint("선수1", "TOP", 0),
                        new TemplateCatalog.PlayerBlueprint("선수2", "JUNGLE", 1)
                    )
                )
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("방 생성 명세에는 픽밴 시간이 필요합니다");
    }
}
