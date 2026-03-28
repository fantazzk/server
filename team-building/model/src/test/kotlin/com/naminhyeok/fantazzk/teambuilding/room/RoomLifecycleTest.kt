package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.Rules
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RoomLifecycleTest {
    private val auctionSettings =
        RoomSettings(
            mode = TeamBuildingMode.AUCTION,
            rules = Rules(teamCount = 2, teamSize = 3, budget = 300),
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

        assertThat(room.status).isEqualTo(RoomStatus.WAITING)
        assertThat(room.teamLeaders.size).isEqualTo(1)
        assertThat(room.teamLeaders.values.first().nickname).isEqualTo("호스트")
        assertThat(room.teamLeaders.values.first().remainingBudget).isEqualTo(300)
    }

    @Test
    fun `팀장을 추가할 수 있다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertThat(room.teamLeaders.size).isEqualTo(2)
    }

    @Test
    fun `방이 가득 차면 팀장을 추가할 수 없다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertThatThrownBy { room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3") }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `대기 상태가 아니면 팀장을 추가할 수 없다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        assertThatThrownBy { room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3") }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `시작하면 IN_PROGRESS 상태로 전이되고 경매 진행이 초기화된다`() {
        val room =
            createWaitingRoom()
                .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
                .start()

        assertThat(room.status).isEqualTo(RoomStatus.IN_PROGRESS)
        assertThat(room.progression).isInstanceOf(Progression.Auction::class.java)
    }

    @Test
    fun `모든 팀장 자리가 채워지지 않으면 시작할 수 없다`() {
        val room = createWaitingRoom()

        assertThatThrownBy { room.start() }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `드래프트 방 시작 시 픽 순서가 초기화된다`() {
        val draftSettings =
            RoomSettings(
                mode = TeamBuildingMode.DRAFT,
                rules = Rules(teamCount = 2, teamSize = 3, draftOrderStrategy = DraftOrderStrategy.SNAKE),
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
        assertThat(draft.pickOrder).hasSize(4)
    }
}
