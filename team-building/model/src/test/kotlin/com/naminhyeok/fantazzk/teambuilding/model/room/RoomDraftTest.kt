package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomDraftTest {
    private fun createStartedDraftRoom(): Room {
        val settings = RoomSettings(
            mode = TeamBuildingMode.DRAFT,
            teamCount = 2,
            teamSize = 2,
            draftOrderStrategy = DraftOrderStrategy.SNAKE,
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
    fun `pick assigns player to current turn team leader`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft
        val currentTurn = draft.currentTurn()

        val updated = room.pick(currentTurn, "선수1")
        val leader = updated.teamLeaders.first { it.id == currentTurn }
        assertEquals(1, leader.team.size)
        assertEquals("선수1", leader.team.first().name)
    }

    @Test
    fun `pick fails when it is not the team leader's turn`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft
        val notCurrentTurn = draft.pickOrder[1]

        assertThrows<IllegalStateException> {
            room.pick(notCurrentTurn, "선수1")
        }
    }

    @Test
    fun `pick fails when player is not available`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft

        assertThrows<IllegalArgumentException> {
            room.pick(draft.currentTurn(), "존재하지않는선수")
        }
    }

    @Test
    fun `draft completes when all picks are done`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft

        // Snake with 2 teams, picksPerTeam=1: [host, leader2]
        val result = room
            .pick(draft.pickOrder[0], "선수1")
            .pick(draft.pickOrder[1], "선수2")

        assertEquals(RoomStatus.COMPLETED, result.status)
        assertEquals(2, result.result?.teams?.size)
    }

    @Test
    fun `snake draft pick order alternates direction`() {
        val settings = RoomSettings(
            mode = TeamBuildingMode.DRAFT,
            teamCount = 2,
            teamSize = 3,
            draftOrderStrategy = DraftOrderStrategy.SNAKE,
        )
        val players = (1..4).map { Player("선수$it") }
        val room = Room.create(
            id = RoomId(1L),
            code = "XYZ789",
            hostId = TeamLeaderId("A"),
            hostNickname = "팀장A",
            settings = settings,
            playerPool = PlayerPool(players),
        ).addTeamLeader(TeamLeaderId("B"), "팀장B")
            .start()

        val draft = room.progression as Progression.Draft
        // picksPerTeam=2, snake: A, B, B, A
        assertEquals(listOf("A", "B", "B", "A"), draft.pickOrder.map { it.value })
    }
}
