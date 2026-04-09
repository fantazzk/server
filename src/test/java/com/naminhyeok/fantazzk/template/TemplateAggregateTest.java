package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TemplateAggregateTest {
    @Nested
    class 경매_템플릿_생성 {
        @Test
        void 입력한_선수_순서를_aggregate_local_player_index와_이름으로_보관한다() {
            Template template =
                Template.createAuction(
                    "주말 경매전",
                    2,
                    2,
                    300,
                    List.of("선수2", "선수1")
                );

            assertThat(template.getPlayers())
                .extracting("playerIndex", "name")
                .containsExactly(
                    tuple(0, "선수2"),
                    tuple(1, "선수1")
                );
        }

        @Test
        void 템플릿_플레이어는_legacy_display_order_accessor를_노출하지_않는다() {
            assertThat(List.of(TemplatePlayer.class.getDeclaredMethods()).stream().map(Method::getName).toList())
                .doesNotContain("getDisplayOrder");
        }

        @Test
        void 필요한_선수_수를_정확히_강제한다() {
            assertThatThrownBy(() ->
                Template.createAuction(
                    "주말 경매전",
                    2,
                    2,
                    300,
                    List.of("선수1")
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("선수 수는 정확히 2명이어야 합니다");
        }

        @Test
        void 경매_설정을_flat_필드로_노출한다() {
            Template template =
                Template.createAuction(
                    "경매전",
                    2,
                    3,
                    300,
                    List.of("선수1", "선수2", "선수3", "선수4")
                );

            assertThat(template.getName()).isEqualTo("경매전");
            assertThat(template.getMode()).isEqualTo(TemplateMode.AUCTION);
            assertThat(template.getTeamCount()).isEqualTo(2);
            assertThat(template.getTeamSize()).isEqualTo(3);
            assertThat(template.getBudget()).isEqualTo(300);
            assertThat(template.getDraftOrderStrategy()).isNull();
            assertThat(template.getPicksPerTeam()).isEqualTo(2);
        }
    }

    @Nested
    class 드래프트_템플릿_생성 {
        @Test
        void 드래프트_설정을_flat_필드로_노출한다() {
            Template template =
                Template.createDraft(
                    "사내 리그 드래프트",
                    2,
                    2,
                    DraftOrderStrategy.FIXED,
                    List.of("선수1", "선수2")
                );

            assertThat(template.getMode()).isEqualTo(TemplateMode.DRAFT);
            assertThat(template.getBudget()).isNull();
            assertThat(template.getDraftOrderStrategy()).isEqualTo(DraftOrderStrategy.FIXED);
        }
    }
}
