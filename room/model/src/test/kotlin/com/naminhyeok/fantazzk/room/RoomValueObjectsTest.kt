package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
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
            val bid = RoomBid(roomId = 1L, round = 2, teamLeaderId = "leader-1", amount = 40)
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
                    roomBidId = 4L,
                    roomId = 2L,
                    round = 3,
                    teamLeaderId = "leader-2",
                    amount = 55,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
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
            val member = RoomTeamMember(roomId = 1L, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0)
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
                    roomTeamMemberId = 6L,
                    roomId = 3L,
                    teamLeaderId = "leader-3",
                    playerName = "선수6",
                    assignOrder = 2,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
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
}
