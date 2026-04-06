package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.TeamBuildingMode
import com.naminhyeok.fantazzk.template.domain.TemplateConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TemplateConfigurationTest {
    @Nested
    inner class `경매 설정` {
        @Test
        fun `경매 설정은 예산과 필요한 선수 수를 노출한다`() {
            val configuration = TemplateConfiguration.from(TeamBuildingMode.AUCTION, 2, 3, 300, null)

            assertThat(configuration).isEqualTo(TemplateConfiguration.auction(2, 3, 300))
            assertThat(configuration.requiredPlayerCount).isEqualTo(4)
            assertThat(configuration.budget).isEqualTo(300)
            val draftOrderStrategy: DraftOrderStrategy? = configuration.draftOrderStrategy
            assertThat(draftOrderStrategy).isNull()
        }

        @Test
        fun `경매 설정은 구체 필드를 그대로 노출한다`() {
            val configuration = TemplateConfiguration.auction(2, 2, 300)

            assertThat(configuration.mode).isEqualTo(TeamBuildingMode.AUCTION)
            assertThat(configuration.budget).isEqualTo(300)
        }

        @Test
        fun `경매 설정은 예산이 필요하다`() {
            assertThatThrownBy {
                TemplateConfiguration.from(TeamBuildingMode.AUCTION, 2, 3, null, null)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("경매 템플릿에는 예산이 필요합니다")
        }

        @Test
        fun `경매 설정은 드래프트 순서 전략을 가질 수 없다`() {
            assertThatThrownBy {
                TemplateConfiguration.from(TeamBuildingMode.AUCTION, 2, 3, 300, DraftOrderStrategy.SNAKE)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다")
        }
    }

    @Nested
    inner class `드래프트 설정` {
        @Test
        fun `드래프트 설정은 순서 전략과 필요한 선수 수를 노출한다`() {
            val configuration =
                TemplateConfiguration.from(
                    TeamBuildingMode.DRAFT,
                    2,
                    3,
                    null,
                    DraftOrderStrategy.SNAKE,
                )

            assertThat(configuration).isEqualTo(
                TemplateConfiguration.draft(2, 3, DraftOrderStrategy.SNAKE),
            )
            assertThat(configuration.requiredPlayerCount).isEqualTo(4)
            val budget: Int? = configuration.budget
            assertThat(budget).isNull()
            assertThat(configuration.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        }

        @Test
        fun `드래프트 설정은 순서 전략이 필요하다`() {
            assertThatThrownBy {
                TemplateConfiguration.from(TeamBuildingMode.DRAFT, 2, 3, null, null)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("드래프트 템플릿에는 순서 전략이 필요합니다")
        }

        @Test
        fun `드래프트 설정은 예산을 가질 수 없다`() {
            assertThatThrownBy {
                TemplateConfiguration.from(TeamBuildingMode.DRAFT, 2, 3, 300, DraftOrderStrategy.SNAKE)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("드래프트 템플릿에는 예산을 지정할 수 없습니다")
        }
    }

    @Nested
    inner class `공통 검증` {
        @Test
        fun `팀 수는 0보다 커야 한다`() {
            assertThatThrownBy { TemplateConfiguration.auction(0, 2, 300) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("팀 수는 0보다 커야 합니다")
        }

        @Test
        fun `경매 설정의 팀 크기는 0보다 커야 한다`() {
            assertThatThrownBy { TemplateConfiguration.auction(2, 0, 300) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("팀 크기는 0보다 커야 합니다")
        }

        @Test
        fun `팀 크기는 0보다 커야 한다`() {
            assertThatThrownBy {
                TemplateConfiguration.draft(2, 0, DraftOrderStrategy.SNAKE)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("팀 크기는 0보다 커야 합니다")
        }

        @Test
        fun `드래프트 설정의 팀 수는 0보다 커야 한다`() {
            assertThatThrownBy {
                TemplateConfiguration.draft(0, 2, DraftOrderStrategy.SNAKE)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("팀 수는 0보다 커야 합니다")
        }

        @Test
        fun `예산은 0보다 커야 한다`() {
            assertThatThrownBy { TemplateConfiguration.auction(2, 2, 0) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("예산은 0보다 커야 합니다")
        }

        @Test
        fun `드래프트 설정은 구체 필드를 그대로 노출한다`() {
            val configuration = TemplateConfiguration.draft(2, 2, DraftOrderStrategy.FIXED)

            assertThat(configuration.mode).isEqualTo(TeamBuildingMode.DRAFT)
            assertThat(configuration.draftOrderStrategy).isEqualTo(DraftOrderStrategy.FIXED)
            assertThat(configuration.requiredPlayerCount).isEqualTo(2)
        }
    }
}
