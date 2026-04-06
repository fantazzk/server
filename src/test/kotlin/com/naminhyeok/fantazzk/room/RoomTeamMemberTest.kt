@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class RoomTeamMemberTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 팀 멤버는 저장 전 internal entity 식별자 없이 생성된다`() {
            val beforeCreate = Instant.now()
            val member = RoomTeamMember(null, "leader-1", "선수1", 0)
            val afterCreate = Instant.now()

            assertThat(member.id).isNull()
            assertThat(member.teamLeaderId).isEqualTo("leader-1")
            assertThat(member.playerName).isEqualTo("선수1")
            assertThat(member.assignOrder).isZero()
            assertThat(member.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(member.updatedAt).isBetween(beforeCreate, afterCreate)
        }

        @Test
        fun `저장된 팀 멤버는 typed internal entity 식별자를 노출한다`() {
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

            assertThat(member.id).isEqualTo(RoomTeamMemberId(6L))
            assertThat(member.roomId).isEqualTo(3L)
            assertThat(member.teamLeaderId).isEqualTo("leader-3")
            assertThat(member.playerName).isEqualTo("선수6")
            assertThat(member.assignOrder).isEqualTo(2)
            assertThat(member.createdAt).isEqualTo(createdAt)
            assertThat(member.updatedAt).isEqualTo(updatedAt)
        }

        @Test
        fun `public typed 생성자는 0 값 팀 멤버 식별자를 허용하지 않는다`() {
            assertThatThrownBy {
                RoomTeamMember(
                    RoomTeamMemberId(0L),
                    null,
                    "leader-1",
                    "선수1",
                    0,
                    Instant.now(),
                    Instant.now(),
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }
    }
}
