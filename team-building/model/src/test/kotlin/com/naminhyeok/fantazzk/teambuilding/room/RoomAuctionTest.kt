package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomAuctionTest {
    private fun createStartedAuctionRoom(): Room {
        val settings =
            RoomSettings(
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
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
        assertEquals(1, auction.currentBids.size)
        assertEquals(100, auction.currentBids.first().amount)
    }

    @Test
    fun `예산을 초과하여 입찰할 수 없다`() {
        val room = createStartedAuctionRoom()

        assertThrows<IllegalArgumentException> {
            room.placeBid(TeamLeaderId("host"), 301)
        }
    }

    @Test
    fun `진행 중이 아닌 방에서는 입찰할 수 없다`() {
        val room =
            Room.create(
                id = RoomId(1L),
                code = "ABC123",
                hostId = TeamLeaderId("host"),
                hostNickname = "호스트",
                settings = RoomSettings(mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300),
                playerPool = PlayerPool(listOf(Player("선수1"), Player("선수2"))),
            )

        assertThrows<IllegalStateException> {
            room.placeBid(TeamLeaderId("host"), 100)
        }
    }

    @Test
    fun `낙찰 시 선수가 팀에 배정되고 예산이 차감된다`() {
        val room =
            createStartedAuctionRoom()
                .placeBid(TeamLeaderId("host"), 100)
                .placeBid(TeamLeaderId("leader2"), 150)
                .settleCurrentAuction()

        val winner = room.teamLeaders.first { it.id == TeamLeaderId("leader2") }
        assertEquals(1, winner.team.size)
        assertEquals("선수1", winner.team.first().name)
        assertEquals(150, winner.remainingBudget)

        val auction = room.progression as Progression.Auction
        assertEquals(1, auction.history.size)
        assert(auction.history.first().outcome is AuctionResult.Outcome.Sold)
    }

    @Test
    fun `유찰 시 선수가 풀 맨 뒤로 이동한다`() {
        val room =
            createStartedAuctionRoom()
                .settleCurrentAuction()

        val pool = room.playerPool
        val available = pool.players.filter { it.status == PlayerStatus.AVAILABLE }
        assertEquals("선수2", available.first().name)
        assertEquals("선수1", available.last().name)

        val auction = room.progression as Progression.Auction
        assertEquals(1, auction.history.size)
        assert(auction.history.first().outcome is AuctionResult.Outcome.Passed)
    }

    @Test
    fun `모든 팀 정원이 채워지면 방이 완료된다`() {
        val room =
            createStartedAuctionRoom()
                .placeBid(TeamLeaderId("host"), 100)
                .settleCurrentAuction()
                .placeBid(TeamLeaderId("leader2"), 100)
                .settleCurrentAuction()

        assertEquals(RoomStatus.COMPLETED, room.status)
        assertNull(room.playerPool.currentTarget())

        val result = requireNotNull(room.result)
        assertEquals(2, result.teams.size)
    }
}
