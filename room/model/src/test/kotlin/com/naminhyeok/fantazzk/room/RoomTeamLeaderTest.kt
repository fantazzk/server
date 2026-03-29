package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoomTeamLeaderTest {
    @Nested
    inner class `예산 검증` {
        @Test
        fun `잔여 예산 이하로 입찰하면 검증을 통과한다`() {
            val leader = leader(remainingBudget = 100)
            assertThatCode { leader.validateBudget(100) }.doesNotThrowAnyException()
        }

        @Test
        fun `잔여 예산을 초과하면 예외가 발생한다`() {
            val leader = leader(remainingBudget = 100)
            assertThatThrownBy { leader.validateBudget(101) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산이 부족합니다")
        }

        @Test
        fun `예산이 null인 모드에서 검증하면 예외가 발생한다`() {
            val leader = leader(remainingBudget = null)
            assertThatThrownBy { leader.validateBudget(1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산이 존재하지 않습니다")
        }
    }

    private fun leader(remainingBudget: Int?) =
        RoomTeamLeader(roomId = 1L, teamLeaderId = "leader-1", nickname = "팀장", remainingBudget = remainingBudget)
}
