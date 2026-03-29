package com.naminhyeok.fantazzk.template

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TemplateTest {
    @Nested
    inner class `생성 검증` {
        @Test
        fun `유효한 값으로 템플릿을 생성할 수 있다`() {
            val template =
                Template(
                    name = "경매전",
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 5,
                    teamSize = 5,
                    budget = 300,
                )

            assertThat(template.picksPerTeam).isEqualTo(4)
        }

        @Test
        fun `팀 수는 0 이하일 수 없다`() {
            assertThatThrownBy { template(teamCount = 0) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `팀 수는 음수일 수 없다`() {
            assertThatThrownBy { template(teamCount = -1) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `팀 인원은 0 이하일 수 없다`() {
            assertThatThrownBy { template(teamSize = 0) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `예산이 지정되면 0 이하일 수 없다`() {
            assertThatThrownBy { template(budget = 0) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `예산이 음수일 수 없다`() {
            assertThatThrownBy { template(budget = -1) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `예산이 null이면 검증을 생략한다`() {
            assertThatCode { template(budget = null) }
                .doesNotThrowAnyException()
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

    private fun template(
        teamCount: Int = 2,
        teamSize: Int = 2,
        budget: Int? = 300,
    ) = Template(
        name = "테스트",
        mode = TeamBuildingMode.AUCTION,
        teamCount = teamCount,
        teamSize = teamSize,
        budget = budget,
    )
}
