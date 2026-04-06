@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class RoomBidTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 입찰은 저장 전 internal entity 식별자 없이 생성된다`() {
            val beforeCreate = Instant.now()
            val bid = RoomBid(2, "leader-1", 40, beforeCreate, beforeCreate)
            val afterCreate = Instant.now()

            assertThat(bid.id).isNull()
            assertThat(bid.round).isEqualTo(2)
            assertThat(bid.teamLeaderId).isEqualTo("leader-1")
            assertThat(bid.amount).isEqualTo(40)
            assertThat(bid.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(bid.updatedAt).isBetween(beforeCreate, afterCreate)
        }

        @Test
        fun `저장된 입찰은 typed internal entity 식별자를 노출한다`() {
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

            assertThat(bid.id).isEqualTo(RoomBidId(4L))
            assertThat(bid.roomId).isEqualTo(2L)
            assertThat(bid.round).isEqualTo(3)
            assertThat(bid.teamLeaderId).isEqualTo("leader-2")
            assertThat(bid.amount).isEqualTo(55)
            assertThat(bid.createdAt).isEqualTo(createdAt)
            assertThat(bid.updatedAt).isEqualTo(updatedAt)
        }

        @Test
        fun `public typed 생성자는 0 값 입찰 식별자를 허용하지 않는다`() {
            assertThatThrownBy {
                RoomBid(RoomBidId(0L), null, 2, "leader-1", 40, Instant.now(), Instant.now())
            }.isInstanceOf(IllegalArgumentException::class.java)
        }
    }
}
