package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.Rules
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RoomAuctionTest {
    private fun createStartedAuctionRoom(): Room {
        val settings =
            RoomSettings(
                mode = TeamBuildingMode.AUCTION,
                rules = Rules(teamCount = 2, teamSize = 2, budget = 300),
            )
        val players = listOf(Player("선수1"), Player("선수2"))
        return Room.create(
            id = RoomId(1L),
            code = "ABC123",
            hostId = TeamLeaderId("host"),
            hostNickname = "호스트",
            settings = settings,
            playerPool = PlayerPool(players),
        ).addTeamLeader(TeamLeaderId("leader2"), "팀장2")
            .start()
    }

    @Test
    fun `입찰하면 현재 경매에 입찰 내역이 추가된다`() {
        val room =
            createStartedAuctionRoom()
                .placeBid(TeamLeaderId("host"), 100)

        val auction = room.progression as Progression.Auction
        assertThat(auction.currentBids).hasSize(1)
        assertThat(auction.currentBids.first().amount).isEqualTo(100)
    }

    @Test
    fun `예산을 초과하여 입찰할 수 없다`() {
        val room = createStartedAuctionRoom()

        assertThatThrownBy { room.placeBid(TeamLeaderId("host"), 301) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `진행 중이 아닌 방에서는 입찰할 수 없다`() {
        val room =
            Room.create(
                id = RoomId(1L),
                code = "ABC123",
                hostId = TeamLeaderId("host"),
                hostNickname = "호스트",
                settings =
                    RoomSettings(
                        mode = TeamBuildingMode.AUCTION,
                        rules = Rules(teamCount = 2, teamSize = 2, budget = 300),
                    ),
                playerPool = PlayerPool(listOf(Player("선수1"), Player("선수2"))),
            )

        assertThatThrownBy { room.placeBid(TeamLeaderId("host"), 100) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `낙찰 시 선수가 팀에 배정되고 예산이 차감된다`() {
        val room =
            createStartedAuctionRoom()
                .placeBid(TeamLeaderId("host"), 100)
                .placeBid(TeamLeaderId("leader2"), 150)
                .settleCurrentAuction()

        val winner = room.teamLeaders.findById(TeamLeaderId("leader2"))
        assertThat(winner.team).hasSize(1)
        assertThat(winner.team.first().name).isEqualTo("선수1")
        assertThat(winner.remainingBudget).isEqualTo(150)

        val auction = room.progression as Progression.Auction
        assertThat(auction.history).hasSize(1)
        assertThat(auction.history.first().outcome).isInstanceOf(AuctionResult.Outcome.Sold::class.java)
    }

    @Test
    fun `유찰 시 선수가 풀 맨 뒤로 이동한다`() {
        val room =
            createStartedAuctionRoom()
                .settleCurrentAuction()

        val available = room.playerPool.players.filter { it.status == PlayerStatus.AVAILABLE }
        assertThat(available.first().name).isEqualTo("선수2")
        assertThat(available.last().name).isEqualTo("선수1")

        val auction = room.progression as Progression.Auction
        assertThat(auction.history).hasSize(1)
        assertThat(auction.history.first().outcome).isInstanceOf(AuctionResult.Outcome.Passed::class.java)
    }

    @Test
    fun `모든 팀 정원이 채워지면 방이 완료된다`() {
        val room =
            createStartedAuctionRoom()
                .placeBid(TeamLeaderId("host"), 100)
                .settleCurrentAuction()
                .placeBid(TeamLeaderId("leader2"), 100)
                .settleCurrentAuction()

        assertThat(room.status).isEqualTo(RoomStatus.COMPLETED)
        assertThat(room.playerPool.currentTarget()).isNull()
        assertThat(room.result).isNotNull
        assertThat(room.result!!.teams).hasSize(2)
    }
}
