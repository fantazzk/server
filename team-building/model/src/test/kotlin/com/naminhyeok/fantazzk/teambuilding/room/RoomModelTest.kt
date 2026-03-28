package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RoomModelTest {
    @Test
    fun `경매 방을 생성할 수 있다`() {
        val room =
            Room(
                code = "ABC123",
                hostId = "host-1",
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 3,
                budget = 300,
            )

        assertThat(room.isWaiting()).isTrue()
        assertThat(room.isAuction()).isTrue()
        assertThat(room.picksPerTeam).isEqualTo(2)
    }

    @Test
    fun `드래프트 방을 생성할 수 있다`() {
        val room =
            Room(
                code = "DEF456",
                hostId = "host-1",
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.DRAFT,
                teamCount = 2,
                teamSize = 3,
                draftOrderStrategy = DraftOrderStrategy.SNAKE,
            )

        assertThat(room.isDraft()).isTrue()
        assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
    }

    @Test
    fun `RoomIdentity를 생성할 수 있다`() {
        val identity = RoomIdentity.of(1L)
        assertThat(identity.roomId).isEqualTo(1L)
    }

    @Test
    fun `RoomPlayer는 가용 상태를 판별할 수 있다`() {
        val player = RoomPlayer(roomId = 1L, name = "선수1", displayOrder = 0)
        assertThat(player.isAvailable()).isTrue()

        val assigned = RoomPlayer(roomId = 1L, name = "선수1", status = PlayerStatus.ASSIGNED, displayOrder = 0)
        assertThat(assigned.isAvailable()).isFalse()
    }

    @Test
    fun `RoomTeamLeader는 예산을 검증할 수 있다`() {
        val leader = RoomTeamLeader(roomId = 1L, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 100)

        assertThatThrownBy { leader.validateBudget(150) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("예산이 부족합니다")
    }

    @Test
    fun `RoomTeamMember는 배정 기록을 표현한다`() {
        val member = RoomTeamMember(roomId = 1L, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0)
        assertThat(member.playerName).isEqualTo("선수1")
    }

    @Test
    fun `RoomBid는 입찰 기록을 표현한다`() {
        val bid = RoomBid(roomId = 1L, round = 1, teamLeaderId = "leader-1", amount = 150)
        assertThat(bid.amount).isEqualTo(150)
    }
}
