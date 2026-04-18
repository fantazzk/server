package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TemplateConfigurationTest {
    @Test
    void 지원_게임_타입은_리그오브레전드와_오버워치2다() {
        assertThat(GameType.values()).containsExactly(
            GameType.LEAGUE_OF_LEGENDS,
            GameType.OVERWATCH_2
        );
    }

    @Nested
    class 경매_설정 {
        @Test
        void 게임타입과_픽밴시간_최소입찰단위_포지션제한을_노출한다() {
            TemplateConfiguration configuration =
                TemplateConfiguration.auction(GameType.LEAGUE_OF_LEGENDS, 2, 3, 300, 45, 10, 2);

            assertThat(configuration.getGameType()).isEqualTo(GameType.LEAGUE_OF_LEGENDS);
            assertThat(configuration.getMode()).isEqualTo(TemplateCatalog.Mode.AUCTION);
            assertThat(configuration.getBudget()).isEqualTo(300);
            assertThat(configuration.getPickBanTime()).isEqualTo(45);
            assertThat(configuration.getMinBidUnit()).isEqualTo(10);
            assertThat(configuration.getPositionLimit()).isEqualTo(2);
            assertThat(configuration.requiredPlayerCount()).isEqualTo(4);
            assertThat(configuration.getDraftOrderStrategy()).isNull();
        }

        @Test
        void 예산이_필요하다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.LEAGUE_OF_LEGENDS,
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    3,
                    null,
                    45,
                    10,
                    2,
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("경매 템플릿에는 예산이 필요합니다");
        }

        @Test
        void 최소_입찰_단위가_필요하다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.LEAGUE_OF_LEGENDS,
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    3,
                    300,
                    45,
                    null,
                    2,
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("경매 템플릿에는 최소 입찰 단위가 필요합니다");
        }

        @Test
        void 드래프트_순서_전략을_가질_수_없다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.LEAGUE_OF_LEGENDS,
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    3,
                    300,
                    45,
                    10,
                    2,
                    DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
        }
    }

    @Nested
    class 드래프트_설정 {
        @Test
        void 게임타입과_픽밴시간_순서전략과_필요한_선수_수를_노출한다() {
            TemplateConfiguration configuration =
                TemplateConfiguration.draft(GameType.OVERWATCH_2, 2, 3, 30, DraftOrderStrategy.SNAKE);

            assertThat(configuration.getGameType()).isEqualTo(GameType.OVERWATCH_2);
            assertThat(configuration.getMode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
            assertThat(configuration.getBudget()).isNull();
            assertThat(configuration.getPickBanTime()).isEqualTo(30);
            assertThat(configuration.getMinBidUnit()).isNull();
            assertThat(configuration.getPositionLimit()).isNull();
            assertThat(configuration.getDraftOrderStrategy()).isEqualTo(DraftOrderStrategy.SNAKE);
            assertThat(configuration.requiredPlayerCount()).isEqualTo(4);
        }

        @Test
        void 순서_전략이_필요하다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.OVERWATCH_2,
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    null,
                    null,
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("드래프트 템플릿에는 순서 전략이 필요합니다");
        }

        @Test
        void 예산을_가질_수_없다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.OVERWATCH_2,
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    300,
                    30,
                    null,
                    null,
                    DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("드래프트 템플릿에는 예산을 지정할 수 없습니다");
        }

        @Test
        void 최소_입찰_단위를_가질_수_없다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.OVERWATCH_2,
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    10,
                    null,
                    DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("드래프트 템플릿에는 최소 입찰 단위를 지정할 수 없습니다");
        }

        @Test
        void 포지션_제한을_가질_수_없다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    GameType.OVERWATCH_2,
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    null,
                    2,
                    DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("드래프트 템플릿에는 포지션 제한을 지정할 수 없습니다");
        }
    }

    @Nested
    class 공통_검증 {
        @Test
        void 팀_수는_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.auction(GameType.LEAGUE_OF_LEGENDS, 0, 2, 300, 45, 10, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팀 수는 0보다 커야 합니다");
        }

        @Test
        void 팀_크기는_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.draft(GameType.OVERWATCH_2, 2, 0, 30, DraftOrderStrategy.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팀 크기는 0보다 커야 합니다");
        }

        @Test
        void 예산은_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.auction(GameType.LEAGUE_OF_LEGENDS, 2, 2, 0, 45, 10, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예산은 0보다 커야 합니다");
        }

        @Test
        void 픽밴_시간은_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.draft(GameType.OVERWATCH_2, 2, 2, 0, DraftOrderStrategy.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("픽밴 시간은 0보다 커야 합니다");
        }
    }
}
