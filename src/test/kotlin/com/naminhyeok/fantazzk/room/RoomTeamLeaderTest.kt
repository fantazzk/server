@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class RoomTeamLeaderTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 팀장은 기본 식별자와 예산 기본값을 가진다`() {
            val beforeCreate = Instant.now()
            val leader = RoomTeamLeader(roomId = 1L, teamLeaderId = "leader-1", nickname = "팀장")
            val afterCreate = Instant.now()

            assertThat(leader.roomTeamLeaderId).isZero()
            assertThat(leader.roomId).isEqualTo(1L)
            assertThat(leader.teamLeaderId).isEqualTo("leader-1")
            assertThat(leader.nickname).isEqualTo("팀장")
            assertThat(leader.remainingBudget).isNull()
            assertThat(leader.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(leader.updatedAt).isBetween(beforeCreate, afterCreate)
        }

        @Test
        fun `팀장은 선언된 속성을 그대로 노출한다`() {
            val createdAt = Instant.parse("2025-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-01-02T00:00:00Z")

            val leader =
                RoomTeamLeader(
                    roomTeamLeaderId = 3L,
                    roomId = 1L,
                    teamLeaderId = "leader-1",
                    nickname = "팀장",
                    remainingBudget = 120,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            assertThat(leader.roomTeamLeaderId).isEqualTo(3L)
            assertThat(leader.roomId).isEqualTo(1L)
            assertThat(leader.teamLeaderId).isEqualTo("leader-1")
            assertThat(leader.nickname).isEqualTo("팀장")
            assertThat(leader.remainingBudget).isEqualTo(120)
            assertThat(leader.createdAt).isEqualTo(createdAt)
            assertThat(leader.updatedAt).isEqualTo(updatedAt)
        }
    }

    @Nested
    inner class `예산 검증` {
        @Test
        fun `잔여 예산 이하로 입찰하면 검증을 통과한다`() {
            val leader = leader(remainingBudget = 100)
            assertThatCode { leader.requireCanBid(100) }.doesNotThrowAnyException()
        }

        @Test
        fun `잔여 예산을 초과하면 예외가 발생한다`() {
            val leader = leader(remainingBudget = 100)
            assertThatThrownBy { leader.requireCanBid(101) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산이 부족합니다")
        }

        @Test
        fun `예산이 null인 모드에서 검증하면 예외가 발생한다`() {
            val leader = leader(remainingBudget = null)
            assertThatThrownBy { leader.requireCanBid(1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산이 존재하지 않습니다")
        }

        @Test
        fun `음수 금액은 입찰 검증을 통과할 수 없다`() {
            val leader = leader(remainingBudget = 100)

            assertThatThrownBy { leader.requireCanBid(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("금액은 0 이상이어야 합니다")
        }
    }

    @Nested
    inner class `예산 전이` {
        @Test
        fun `팀장은 낙찰 금액만큼 예산을 차감한다`() {
            val leader = leader(remainingBudget = 100)

            val spent = leader.spend(30)

            assertThat(spent.remainingBudget).isEqualTo(70)
            assertThat(spent.roomTeamLeaderId).isEqualTo(leader.roomTeamLeaderId)
            assertThat(spent.teamLeaderId).isEqualTo(leader.teamLeaderId)
        }

        @Test
        fun `예산을 초과하는 금액은 차감할 수 없다`() {
            val leader = leader(remainingBudget = 100)

            assertThatThrownBy { leader.spend(101) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산이 부족합니다")
        }

        @Test
        fun `음수 금액은 차감할 수 없다`() {
            val leader = leader(remainingBudget = 100)

            assertThatThrownBy { leader.spend(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("금액은 0 이상이어야 합니다")
        }
    }

    @Nested
    inner class `aggregate 규칙 위임` {
        @Test
        fun `팀장은 aggregate에서 예산 규칙을 그대로 따른다`() {
            val leader =
                RoomTeamLeader(
                    roomTeamLeaderId = 12L,
                    roomId = 2L,
                    teamLeaderId = "leader-12",
                    nickname = "주장",
                    remainingBudget = 100,
                    createdAt = Instant.parse("2025-02-03T00:00:00Z"),
                    updatedAt = Instant.parse("2025-02-04T00:00:00Z"),
                )

            assertThatCode { leader.requireCanBid(60) }.doesNotThrowAnyException()
            assertThatCode { leader.validateBudget(100) }.doesNotThrowAnyException()

            val spent = leader.spend(30)

            assertThat(spent.remainingBudget).isEqualTo(70)
            assertThat(spent.teamLeaderId).isEqualTo("leader-12")
        }
    }

    private fun leader(remainingBudget: Int?) =
        RoomTeamLeader(roomId = 1L, teamLeaderId = "leader-1", nickname = "팀장", remainingBudget = remainingBudget)
}
