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
    fun `방 생성 시 WAITING 상태이며 호스트가 첫 번째 팀장이다`() {
        val room = createWaitingRoom()

        assertEquals(RoomStatus.WAITING, room.status)
        assertEquals(1, room.teamLeaders.size)
        assertEquals("호스트", room.teamLeaders.first().nickname)
        assertEquals(300, room.teamLeaders.first().remainingBudget)
    }

    @Test
    fun `팀장을 추가할 수 있다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertEquals(2, room.teamLeaders.size)
    }

    @Test
    fun `방이 가득 차면 팀장을 추가할 수 없다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertThrows<IllegalStateException> {
            room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3")
        }
    }

    @Test
    fun `대기 상태가 아니면 팀장을 추가할 수 없다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        assertThrows<IllegalStateException> {
            room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3")
        }
    }

    @Test
    fun `시작하면 IN_PROGRESS 상태로 전이되고 경매 진행이 초기화된다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        assertEquals(RoomStatus.IN_PROGRESS, room.status)
        assert(room.progression is Progression.Auction)
    }

    @Test
    fun `모든 팀장 자리가 채워지지 않으면 시작할 수 없다`() {
        val room = createWaitingRoom()

        assertThrows<IllegalStateException> {
            room.start()
        }
    }

    @Test
    fun `드래프트 방 시작 시 픽 순서가 초기화된다`() {
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
        assertEquals(4, draft.pickOrder.size)
    }
}
