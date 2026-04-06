@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class RoomValueObjectsTest {
    @Nested
    inner class `경매 결과 계약` {
        @Test
        fun `경매 결과는 판매와 유찰을 모두 표현한다`() {
            assertThat(AuctionOutcome.entries).containsExactly(AuctionOutcome.SOLD, AuctionOutcome.PASSED)
        }
    }

    @Nested
    inner class `입찰 계약` {
        @Test
        fun `입찰은 기본 식별자와 감사 시각을 가진다`() {
            val beforeCreate = Instant.now()
            val bid = RoomBid(1L, 2, "leader-1", 40, beforeCreate, beforeCreate)
            val afterCreate = Instant.now()

            assertThat(bid.roomBidId).isZero()
            assertThat(bid.roomId).isEqualTo(1L)
            assertThat(bid.round).isEqualTo(2)
            assertThat(bid.teamLeaderId).isEqualTo("leader-1")
            assertThat(bid.amount).isEqualTo(40)
            assertThat(bid.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(bid.updatedAt).isBetween(beforeCreate, afterCreate)
        }

        @Test
        fun `입찰은 선언된 속성을 그대로 노출한다`() {
            val createdAt = Instant.parse("2025-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-01-02T00:00:00Z")

            val bid =
                RoomBid(
                    4L,
                    2L,
                    3,
                    "leader-2",
                    55,
                    createdAt,
                    updatedAt,
                )

            assertThat(bid.roomBidId).isEqualTo(4L)
            assertThat(bid.roomId).isEqualTo(2L)
            assertThat(bid.round).isEqualTo(3)
            assertThat(bid.teamLeaderId).isEqualTo("leader-2")
            assertThat(bid.amount).isEqualTo(55)
            assertThat(bid.createdAt).isEqualTo(createdAt)
            assertThat(bid.updatedAt).isEqualTo(updatedAt)
        }
    }

    @Nested
    inner class `팀 멤버 계약` {
        @Test
        fun `팀 멤버는 기본 식별자와 감사 시각을 가진다`() {
            val beforeCreate = Instant.now()
            val member = RoomTeamMember(1L, "leader-1", "선수1", 0)
            val afterCreate = Instant.now()

            assertThat(member.roomTeamMemberId).isZero()
            assertThat(member.roomId).isEqualTo(1L)
            assertThat(member.teamLeaderId).isEqualTo("leader-1")
            assertThat(member.playerName).isEqualTo("선수1")
            assertThat(member.assignOrder).isZero()
            assertThat(member.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(member.updatedAt).isBetween(beforeCreate, afterCreate)
        }

        @Test
        fun `팀 멤버는 선언된 속성을 그대로 노출한다`() {
            val createdAt = Instant.parse("2025-02-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-02-02T00:00:00Z")

            val member =
                RoomTeamMember(
                    6L,
                    3L,
                    "leader-3",
                    "선수6",
                    2,
                    createdAt,
                    updatedAt,
                )

            assertThat(member.roomTeamMemberId).isEqualTo(6L)
            assertThat(member.roomId).isEqualTo(3L)
            assertThat(member.teamLeaderId).isEqualTo("leader-3")
            assertThat(member.playerName).isEqualTo("선수6")
            assertThat(member.assignOrder).isEqualTo(2)
            assertThat(member.createdAt).isEqualTo(createdAt)
            assertThat(member.updatedAt).isEqualTo(updatedAt)
        }
    }

    @Nested
    inner class `예산 상태 계약` {
        @Test
        fun `예산 상태는 null 여부에 따라 선택적으로 생성된다`() {
            assertThat(BudgetState.from(70)?.remainingBudget).isEqualTo(70)
            assertThat(BudgetState.from(null)).isNull()
        }

        @Test
        fun `예산 상태는 음수를 거부하고 필수 예산을 강제한다`() {
            assertThatThrownBy { BudgetState(-1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산은 0 이상이어야 합니다")

            val state = BudgetState.requireFrom(30)

            assertThat(state.remainingBudget).isEqualTo(30)
            assertThatThrownBy { BudgetState.requireFrom(null) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("예산이 존재하지 않습니다")
        }
    }
}
