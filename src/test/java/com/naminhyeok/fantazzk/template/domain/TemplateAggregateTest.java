package com.naminhyeok.fantazzk.template.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TemplateAggregateTest {
    @Nested
    class 경매_템플릿_생성 {
        @Test
        void 입력한_선수의_이름_포지션_표시순서를_보관한다() {
            Template template =
                Template.createAuction(
                    "주말 경매전",
                    TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
                    2,
                    2,
                    300,
                    45,
                    10,
                    1,
                    List.of(
                        new TemplatePlayer("선수2", "JUNGLE", 0),
                        new TemplatePlayer("선수1", "TOP", 1)
                    )
                );

            assertThat(template.getPlayers())
                .extracting(TemplatePlayer::displayOrder, TemplatePlayer::name, TemplatePlayer::position)
                .containsExactly(
                    tuple(0, "선수2", "JUNGLE"),
                    tuple(1, "선수1", "TOP")
                );
        }

        @Test
        void 필요한_선수_수를_정확히_강제한다() {
            assertThatThrownBy(() ->
                Template.createAuction(
                    "주말 경매전",
                    TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
                    2,
                    2,
                    300,
                    45,
                    10,
                    1,
                    List.of(new TemplatePlayer("선수1", "TOP", 0))
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("선수 수는 정확히 2명이어야 합니다");
        }

        @Test
        void 선택한_게임_타입에서_지원하지_않는_포지션이면_거부한다() {
            assertThatThrownBy(() ->
                Template.createAuction(
                    "주말 경매전",
                    TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
                    2,
                    2,
                    300,
                    45,
                    10,
                    1,
                    List.of(
                        new TemplatePlayer("선수1", "TANK", 0),
                        new TemplatePlayer("선수2", "SUPPORT", 1)
                    )
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("LEAGUE_OF_LEGENDS 게임은 TANK 포지션을 지원하지 않습니다");
        }

        @Test
        void 경매_설정을_flat_필드로_노출한다() {
            Template template =
                Template.createAuction(
                    "경매전",
                    TemplateCatalog.GameType.LEAGUE_OF_LEGENDS,
                    2,
                    3,
                    300,
                    45,
                    10,
                    2,
                    List.of(
                        new TemplatePlayer("선수1", "TOP", 0),
                        new TemplatePlayer("선수2", "JUNGLE", 1),
                        new TemplatePlayer("선수3", "MID", 2),
                        new TemplatePlayer("선수4", "ADC", 3)
                    )
                );

            assertThat(template.getName()).isEqualTo("경매전");
            assertThat(template.getGameType()).isEqualTo(TemplateCatalog.GameType.LEAGUE_OF_LEGENDS);
            assertThat(template.getMode()).isEqualTo(TemplateCatalog.Mode.AUCTION);
            assertThat(template.getTeamCount()).isEqualTo(2);
            assertThat(template.getTeamSize()).isEqualTo(3);
            assertThat(template.getBudget()).isEqualTo(300);
            assertThat(template.getPickBanTime()).isEqualTo(45);
            assertThat(template.getMinBidUnit()).isEqualTo(10);
            assertThat(template.getPositionLimit()).isEqualTo(2);
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
                    TemplateCatalog.GameType.OVERWATCH_2,
                    2,
                    2,
                    30,
                    TemplateCatalog.DraftOrderStrategy.FIXED,
                    List.of(
                        new TemplatePlayer("선수1", "TANK", 0),
                        new TemplatePlayer("선수2", "SUPPORT", 1)
                    )
                );

            assertThat(template.getGameType()).isEqualTo(TemplateCatalog.GameType.OVERWATCH_2);
            assertThat(template.getMode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
            assertThat(template.getBudget()).isNull();
            assertThat(template.getPickBanTime()).isEqualTo(30);
            assertThat(template.getMinBidUnit()).isNull();
            assertThat(template.getPositionLimit()).isNull();
            assertThat(template.getDraftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.FIXED);
        }
    }
}
