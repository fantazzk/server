@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.reflect.full.memberProperties

class RoomTeamMemberTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 팀 멤버는 저장 전 internal entity 식별자 없이 생성된다`() {
            val beforeCreate = Instant.now()
            val member = RoomTeamMember(teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0)
            val afterCreate = Instant.now()

            assertThat(readEntityId(member)).isNull()
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
                    roomTeamMemberId = 6L,
                    roomId = 3L,
                    teamLeaderId = "leader-3",
                    playerName = "선수6",
                    assignOrder = 2,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            assertThat(readEntityId(member).toString()).isEqualTo("RoomTeamMemberId(value=6)")
            assertThat(member.roomId).isEqualTo(3L)
            assertThat(member.teamLeaderId).isEqualTo("leader-3")
            assertThat(member.playerName).isEqualTo("선수6")
            assertThat(member.assignOrder).isEqualTo(2)
            assertThat(member.createdAt).isEqualTo(createdAt)
            assertThat(member.updatedAt).isEqualTo(updatedAt)
        }
    }

    private fun readEntityId(member: RoomTeamMember): Any? =
        RoomTeamMember::class.memberProperties
            .singleOrNull { it.name == "id" }
            ?.getter
            ?.call(member)
}
