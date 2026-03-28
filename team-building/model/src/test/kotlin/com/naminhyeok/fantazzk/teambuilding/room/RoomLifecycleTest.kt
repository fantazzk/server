package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomLifecycleTest {
    private val auctionSettings =
        RoomSettings(
            mode = TeamBuildingMode.AUCTION,
            teamCount = 2,
            teamSize = 3,
            budget = 300,
        )
    private val players = listOf(Player("선수1"), Player("선수2"), Player("선수3"), Player("선수4"))

    private fun createWaitingRoom(): Room =
        Room.create(
            id = RoomId(1L),
            code = "ABC123",
            hostId = TeamLeaderId("host-1"),
            hostNickname = "호스트",
            settings = auctionSettings,
            playerPool = PlayerPool(players),
        )

    @Test
    fun `create room initializes in WAITING status with host as first team leader`() {
        val room = createWaitingRoom()

        assertEquals(RoomStatus.WAITING, room.status)
        assertEquals(1, room.teamLeaders.size)
        assertEquals("호스트", room.teamLeaders.first().nickname)
        assertEquals(300, room.teamLeaders.first().remainingBudget)
    }

    @Test
    fun `addTeamLeader adds a new team leader`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertEquals(2, room.teamLeaders.size)
    }

    @Test
    fun `addTeamLeader fails when room is full`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertThrows<IllegalStateException> {
            room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3")
        }
    }

    @Test
    fun `addTeamLeader fails when not in WAITING status`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        assertThrows<IllegalStateException> {
            room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3")
        }
    }

    @Test
    fun `start transitions to IN_PROGRESS and initializes progression`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        assertEquals(RoomStatus.IN_PROGRESS, room.status)
        assert(room.progression is Progression.Auction)
    }

    @Test
    fun `start fails when not all team leader slots are filled`() {
        val room = createWaitingRoom()

        assertThrows<IllegalStateException> {
            room.start()
        }
    }

    @Test
    fun `start draft room initializes draft progression with pick order`() {
        val draftSettings =
            RoomSettings(
                mode = TeamBuildingMode.DRAFT,
                teamCount = 2,
                teamSize = 3,
                draftOrderStrategy = DraftOrderStrategy.SNAKE,
            )
        val room =
            Room.create(
                id = RoomId(2L),
                code = "DEF456",
                hostId = TeamLeaderId("host-1"),
                hostNickname = "호스트",
                settings = draftSettings,
                playerPool = PlayerPool(players),
            ).addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        val draft = room.progression as Progression.Draft
        // Snake: picksPerTeam=2 -> Round1: host,leader2  Round2: leader2,host
        assertEquals(4, draft.pickOrder.size)
    }
}
