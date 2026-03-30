package com.naminhyeok.fantazzk.template

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class TemplateTest {
    @Nested
    inner class `템플릿 생성` {
        @Test
        fun `Template create는 강타입 설정을 flat 필드로 노출한다`() {
            val template =
                Template.create(
                    name = "경매전",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 3, budgetValue = 300),
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
        fun `템플릿은 선언된 식별자와 시간 값을 그대로 노출한다`() {
            val createdAt = Instant.parse("2025-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-01-02T00:00:00Z")

            val template =
                Template(
                    templateId = 9L,
                    name = "드래프트전",
                    templateConfiguration =
                        TemplateConfiguration.Draft(
                            teamCount = 2,
                            teamSize = 2,
                            strategy = DraftOrderStrategy.FIXED,
                        ),
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            assertThat(template.templateId).isEqualTo(9L)
            assertThat(template.createdAt).isEqualTo(createdAt)
            assertThat(template.updatedAt).isEqualTo(updatedAt)
            assertThat(template.draftOrderStrategy).isEqualTo(DraftOrderStrategy.FIXED)
        }
    }

    @Nested
    inner class `파생 값` {
        @Test
        fun `TemplateModel configuration은 경매 템플릿을 강타입 설정으로 복원한다`() {
            val template =
                Template.create(
                    name = "경매전",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 3, budgetValue = 300),
                )

            assertThat(template.configuration)
                .isEqualTo(TemplateConfiguration.Auction(teamCount = 2, teamSize = 3, budgetValue = 300))
        }

        @Test
        fun `TemplateModel configuration은 드래프트 템플릿을 강타입 설정으로 복원한다`() {
            val template =
                object : TemplateModel {
                    override val templateId = 5L
                    override val name = "드래프트전"
                    override val mode = TeamBuildingMode.DRAFT
                    override val teamCount = 2
                    override val teamSize = 2
                    override val budget: Int? = null
                    override val draftOrderStrategy = DraftOrderStrategy.SNAKE
                    override val createdAt = Instant.parse("2025-01-01T00:00:00Z")
                    override val updatedAt = Instant.parse("2025-01-02T00:00:00Z")
                }

            assertThat(template.configuration)
                .isEqualTo(TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE))
        }

        @Test
        fun `picksPerTeam은 teamSize에서 1을 뺀 값이다`() {
            val template =
                Template.create(
                    name = "드래프트전",
                    configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 5, strategy = DraftOrderStrategy.SNAKE),
                )

            assertThat(template.picksPerTeam).isEqualTo(4)
        }

        @Test
        fun `requireValidRoster는 exact player count를 만족하면 통과한다`() {
            val template =
                Template.create(
                    name = "경매전",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
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
                Template.create(
                    name = "경매전",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
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
        fun `TemplateIdentity를 생성할 수 있다`() {
            val identity = TemplateIdentity.of(42L)

            assertThat(identity.templateId).isEqualTo(42L)
        }
    }
}
