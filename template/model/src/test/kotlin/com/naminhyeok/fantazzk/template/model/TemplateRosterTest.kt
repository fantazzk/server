package com.naminhyeok.fantazzk.template.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class TemplateRosterTest {
    @Test
    fun `일급 컬렉션은 exact player count를 만족하면 입력 순서를 유지한다`() {
        val roster = TemplateRoster.exactlyRequired(listOf("선수B", "선수A"), requiredPlayerCount = 2)

        assertThat(roster.playerNames()).containsExactly("선수B", "선수A")
    }

    @Test
    fun `일급 컬렉션은 exact player count를 강제한다`() {
        assertThatThrownBy {
            TemplateRoster.exactlyRequired(listOf("선수1"), requiredPlayerCount = 2)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("선수 수는 정확히 2명이어야 합니다")
    }

    @Test
    fun `일급 컬렉션은 템플릿 선수 목록으로 변환할 때 displayOrder를 보존한다`() {
        val roster = TemplateRoster.exactlyRequired(listOf("선수B", "선수A"), requiredPlayerCount = 2)

        val players = roster.toPlayers(templateId = 11L)

        assertThat(players.map { Triple(it.templateId, it.name, it.displayOrder) })
            .containsExactly(
                Triple(11L, "선수B", 0),
                Triple(11L, "선수A", 1),
            )
    }
}
