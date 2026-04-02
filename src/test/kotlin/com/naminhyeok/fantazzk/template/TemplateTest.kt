package com.naminhyeok.fantazzk.template

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
        fun `Template createAuction은 선수 컬렉션을 displayOrder 순서로 보관한다`() {
            val template =
                Template.createAuction(
                    name = "통합 템플릿",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수2", "선수1"),
                )

            assertThat(template.players().map { it.name }).containsExactly("선수1", "선수2")
            assertThat(template.players().map { it.displayOrder }).containsExactly(0, 1)
        }

        @Test
        fun `Template createAuction은 필요한 선수 수를 정확히 강제한다`() {
            assertThatThrownBy {
                Template.createAuction(
                    name = "통합 템플릿",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1"),
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("선수 수는 정확히 2명이어야 합니다")
        }

        @Test
        fun `Template createAuction은 강타입 설정을 flat 필드로 노출한다`() {
            val template =
                Template.createAuction(
                    name = "경매전",
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2", "선수3", "선수4"),
                )

            assertThat(template.templateId).isZero()
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
                Template.createDraft(
                    name = "드래프트전",
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.FIXED,
                    playerNames = listOf("선수1", "선수2"),
                ).assignId(TemplateId(9L))

            assertThat(template.templateId).isEqualTo(9L)
            assertThat(template.createdAt).isNotNull()
            assertThat(template.updatedAt).isNotNull()
            assertThat(template.draftOrderStrategy).isEqualTo(DraftOrderStrategy.FIXED)
        }

        @Test
        fun `템플릿은 생성 완료 이벤트를 내부 pending event로 기록할 수 있다`() {
            val template =
                Template.createAuction(
                    name = "경매전",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2"),
                )
                    .assignId(TemplateId(1L))

            val recorded = template.recordCreated()

            assertThat(recorded.drainEvents()).containsExactly(
                TemplateCreated(
                    templateId = 1L,
                    name = "경매전",
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    draftOrderStrategy = null,
                    players =
                        listOf(
                            TemplatePlayerCreated(name = "선수1", displayOrder = 0),
                            TemplatePlayerCreated(name = "선수2", displayOrder = 1),
                        ),
                ),
            )
        }
    }

    @Nested
    inner class `파생 값` {
        @Test
        fun `경매 템플릿 configuration은 강타입 설정을 복원한다`() {
            val template =
                Template.createAuction(
                    name = "경매전",
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2", "선수3", "선수4"),
                )

            assertThat(template.configuration)
                .isEqualTo(TemplateConfiguration.auction(teamCount = 2, teamSize = 3, budget = 300))
        }

        @Test
        fun `드래프트 템플릿 configuration은 강타입 설정을 복원한다`() {
            val template =
                Template.createDraft(
                    name = "드래프트전",
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.SNAKE,
                    playerNames = listOf("선수1", "선수2"),
                ).assignId(TemplateId(5L))

            assertThat(template.configuration)
                .isEqualTo(TemplateConfiguration.draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE))
        }

        @Test
        fun `picksPerTeam은 teamSize에서 1을 뺀 값이다`() {
            val template =
                Template.createDraft(
                    name = "드래프트전",
                    teamCount = 2,
                    teamSize = 5,
                    strategy = DraftOrderStrategy.SNAKE,
                    playerNames = listOf("선수1", "선수2", "선수3", "선수4", "선수5", "선수6", "선수7", "선수8"),
                )

            assertThat(template.picksPerTeam).isEqualTo(4)
        }

        @Test
        fun `requireValidRoster는 exact player count를 만족하면 통과한다`() {
            val template =
                Template.createAuction(
                    name = "경매전",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2"),
                )

            val players =
                listOf(
                    TemplatePlayer(templateId = 1L, name = "선수B", displayOrder = 1),
                    TemplatePlayer(templateId = 1L, name = "선수A", displayOrder = 0),
                )

            assertThatCode { template.requireValidRoster(players) }.doesNotThrowAnyException()
        }

        @Test
        fun `requireValidRoster는 exact player count를 만족하지 않으면 예외를 던진다`() {
            val template =
                Template.createAuction(
                    name = "경매전",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2"),
                )

            val players = listOf(TemplatePlayer(templateId = 1L, name = "선수A", displayOrder = 0))

            assertThatThrownBy { template.requireValidRoster(players) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("선수 수는 정확히 2명이어야 합니다")
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `Template getId는 TemplateId를 반환한다`() {
            val template =
                Template.createAuction(
                    name = "테스트",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2"),
                ).assignId(TemplateId(42L))

            assertThat(template.getId()).isEqualTo(TemplateId(42L))
        }
    }
}
