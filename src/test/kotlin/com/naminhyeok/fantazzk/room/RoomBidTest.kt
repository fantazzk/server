@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.reflect.full.memberProperties

class RoomBidTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 입찰은 저장 전 internal entity 식별자 없이 생성된다`() {
            val beforeCreate = Instant.now()
            val bid = RoomBid(round = 2, teamLeaderId = "leader-1", amount = 40)
            val afterCreate = Instant.now()

            assertThat(readEntityId(bid)).isNull()
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
                    roomBidId = 4L,
                    roomId = 2L,
                    round = 3,
                    teamLeaderId = "leader-2",
                    amount = 55,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            assertThat(readEntityId(bid).toString()).isEqualTo("RoomBidId(value=4)")
            assertThat(bid.roomId).isEqualTo(2L)
            assertThat(bid.round).isEqualTo(3)
            assertThat(bid.teamLeaderId).isEqualTo("leader-2")
            assertThat(bid.amount).isEqualTo(55)
            assertThat(bid.createdAt).isEqualTo(createdAt)
            assertThat(bid.updatedAt).isEqualTo(updatedAt)
        }
    }

    private fun readEntityId(bid: RoomBid): Any? =
        RoomBid::class.memberProperties
            .singleOrNull { it.name == "id" }
            ?.getter
            ?.call(bid)
}
