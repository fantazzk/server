package com.naminhyeok.fantazzk.template.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naminhyeok.fantazzk.template.TemplateCatalog;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TemplateConfigurationTest {
    @Nested
    class 경매_설정 {
        @Test
        void 픽밴시간과_최소입찰단위를_노출한다() {
            TemplateConfiguration configuration =
                TemplateConfiguration.auction(2, 3, 300, 45, 10);

            assertThat(configuration.getMode()).isEqualTo(TemplateCatalog.Mode.AUCTION);
            assertThat(configuration.getBudget()).isEqualTo(300);
            assertThat(configuration.getPickBanTime()).isEqualTo(45);
            assertThat(configuration.getMinBidUnit()).isEqualTo(10);
            assertThat(configuration.requiredPlayerCount()).isEqualTo(4);
            assertThat(configuration.getDraftOrderStrategy()).isNull();
        }

        @Test
        void 예산이_필요하다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    3,
                    null,
                    45,
                    10,
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
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    3,
                    300,
                    45,
                    null,
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
                    TemplateCatalog.Mode.AUCTION,
                    2,
                    3,
                    300,
                    45,
                    10,
                    TemplateCatalog.DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
        }
    }

    @Nested
    class 드래프트_설정 {
        @Test
        void 픽밴시간과_순서전략과_필요한_선수_수를_노출한다() {
            TemplateConfiguration configuration =
                TemplateConfiguration.draft(2, 3, 30, TemplateCatalog.DraftOrderStrategy.SNAKE);

            assertThat(configuration.getMode()).isEqualTo(TemplateCatalog.Mode.DRAFT);
            assertThat(configuration.getBudget()).isNull();
            assertThat(configuration.getPickBanTime()).isEqualTo(30);
            assertThat(configuration.getMinBidUnit()).isNull();
            assertThat(configuration.getDraftOrderStrategy()).isEqualTo(TemplateCatalog.DraftOrderStrategy.SNAKE);
            assertThat(configuration.requiredPlayerCount()).isEqualTo(4);
        }

        @Test
        void 순서_전략이_필요하다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
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
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    300,
                    30,
                    null,
                    TemplateCatalog.DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("드래프트 템플릿에는 예산을 지정할 수 없습니다");
        }

        @Test
        void 최소_입찰_단위를_가질_수_없다() {
            assertThatThrownBy(() ->
                TemplateConfiguration.from(
                    TemplateCatalog.Mode.DRAFT,
                    2,
                    3,
                    null,
                    30,
                    10,
                    TemplateCatalog.DraftOrderStrategy.SNAKE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("드래프트 템플릿에는 최소 입찰 단위를 지정할 수 없습니다");
        }
    }

    @Nested
    class 공통_검증 {
        @Test
        void 팀_수는_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.auction(0, 2, 300, 45, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팀 수는 0보다 커야 합니다");
        }

        @Test
        void 팀_크기는_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.draft(2, 0, 30, TemplateCatalog.DraftOrderStrategy.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팀 크기는 0보다 커야 합니다");
        }

        @Test
        void 예산은_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.auction(2, 2, 0, 45, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예산은 0보다 커야 합니다");
        }

        @Test
        void 픽밴_시간은_0보다_커야_한다() {
            assertThatThrownBy(() -> TemplateConfiguration.draft(2, 2, 0, TemplateCatalog.DraftOrderStrategy.FIXED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("픽밴 시간은 0보다 커야 합니다");
        }
    }
}
