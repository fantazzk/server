package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RulesTest {
    @Test
    fun `팀 수는 0 이하일 수 없다`() {
        assertThatThrownBy { Rules(teamCount = 0, teamSize = 5) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `팀 인원은 0 이하일 수 없다`() {
        assertThatThrownBy { Rules(teamCount = 5, teamSize = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `예산이 지정되면 0 이하일 수 없다`() {
        assertThatThrownBy { Rules(teamCount = 5, teamSize = 5, budget = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `유효한 경매 규칙을 생성할 수 있다`() {
        assertThatCode { Rules(teamCount = 5, teamSize = 5, budget = 300) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `유효한 드래프트 규칙을 생성할 수 있다`() {
        assertThatCode { Rules(teamCount = 5, teamSize = 5, draftOrderStrategy = DraftOrderStrategy.SNAKE) }
            .doesNotThrowAnyException()
    }
}
