package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.TeamBuildingMode
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.domain.TemplateConfiguration
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TemplateTest {
    @Nested
    inner class `템플릿 생성` {
        @Test
        fun `Template createAuction은 입력한 선수 순서를 displayOrder로 보관한다`() {
            val template =
                Template.createAuction("통합 템플릿", 2, 2, 300, listOf("선수2", "선수1"))

            assertThat(template.players().map { it.name }).containsExactly("선수2", "선수1")
            assertThat(template.players().map { it.displayOrder }).containsExactly(0, 1)
        }

        @Test
        fun `Template createAuction은 필요한 선수 수를 정확히 강제한다`() {
            assertThatThrownBy {
                Template.createAuction("통합 템플릿", 2, 2, 300, listOf("선수1"))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("선수 수는 정확히 2명이어야 합니다")
        }

        @Test
        fun `Template createAuction은 강타입 설정을 flat 필드로 노출한다`() {
            val template =
                Template.createAuction("경매전", 2, 3, 300, listOf("선수1", "선수2", "선수3", "선수4"))

            assertThat(template.name).isEqualTo("경매전")
            assertThat(template.mode).isEqualTo(TeamBuildingMode.AUCTION)
            assertThat(template.teamCount).isEqualTo(2)
            assertThat(template.teamSize).isEqualTo(3)
            assertThat(template.budget).isEqualTo(300)
            assertThat(template.draftOrderStrategy).isNull()
        }

        @Test
        fun `템플릿은 생성 시각과 수정 시각을 노출한다`() {
            val template =
                Template.createDraft("드래프트전", 2, 2, DraftOrderStrategy.FIXED, listOf("선수1", "선수2"))
                    .assignId(templateId(9))

            assertThat(template.templateId).isEqualTo(templateId(9))
            assertThat(template.createdAt).isNotNull()
            assertThat(template.updatedAt).isNotNull()
            assertThat(template.draftOrderStrategy).isEqualTo(DraftOrderStrategy.FIXED)
        }
    }

    @Nested
    inner class `파생 값` {
        @Test
        fun `경매 템플릿 configuration은 강타입 설정을 복원한다`() {
            val template =
                Template.createAuction("경매전", 2, 3, 300, listOf("선수1", "선수2", "선수3", "선수4"))

            assertThat(template.configuration)
                .isEqualTo(TemplateConfiguration.auction(2, 3, 300))
        }

        @Test
        fun `드래프트 템플릿 configuration은 강타입 설정을 복원한다`() {
            val template =
                Template.createDraft("드래프트전", 2, 2, DraftOrderStrategy.SNAKE, listOf("선수1", "선수2"))
                    .assignId(templateId(5))

            assertThat(template.configuration)
                .isEqualTo(TemplateConfiguration.draft(2, 2, DraftOrderStrategy.SNAKE))
        }

        @Test
        fun `picksPerTeam은 teamSize에서 1을 뺀 값이다`() {
            val template =
                Template.createDraft("드래프트전", 2, 5, DraftOrderStrategy.SNAKE, listOf("선수1", "선수2", "선수3", "선수4", "선수5", "선수6", "선수7", "선수8"))

            assertThat(template.picksPerTeam).isEqualTo(4)
        }

        @Test
        fun `requireValidRoster는 exact player count를 만족하면 통과한다`() {
            val template =
                Template.createAuction("경매전", 2, 2, 300, listOf("선수1", "선수2"))

            val players =
                listOf(
                    TemplatePlayer(templateId(1), "선수B", 1),
                    TemplatePlayer(templateId(1), "선수A", 0),
                )

            assertThatCode { template.requireValidRoster(players) }.doesNotThrowAnyException()
        }

        @Test
        fun `requireValidRoster는 exact player count를 만족하지 않으면 예외를 던진다`() {
            val template =
                Template.createAuction("경매전", 2, 2, 300, listOf("선수1", "선수2"))

            val players = listOf(TemplatePlayer(templateId(1), "선수A", 0))

            assertThatThrownBy { template.requireValidRoster(players) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("선수 수는 정확히 2명이어야 합니다")
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `Template id는 TemplateId를 반환한다`() {
            val template =
                Template.createAuction("테스트", 2, 2, 300, listOf("선수1", "선수2"))
                    .assignId(templateId(42))

            assertThat(template.id).isEqualTo(templateId(42))
        }
    }

    private fun templateId(number: Long): TemplateId = TemplateId.from(templateIdText(number))

    private fun templateIdText(number: Long): String = "00000000-0000-0000-0000-${number.toString().padStart(12, '0')}"
}
