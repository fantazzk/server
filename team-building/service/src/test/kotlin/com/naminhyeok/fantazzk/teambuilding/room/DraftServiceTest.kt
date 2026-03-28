package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.support.InMemoryRoomTeamMemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DraftServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var memberRepo: InMemoryRoomTeamMemberRepository
    private lateinit var draftService: DraftService

    private lateinit var roomCode: String
    private var roomId: Long = 0L

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        memberRepo = InMemoryRoomTeamMemberRepository()
        draftService = DraftServiceImpl(roomRepo, leaderRepo, playerRepo, memberRepo)

        val room =
            roomRepo.save(
                Room(
                    code = "DRAFT1",
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 0,
                ),
            )
        roomCode = room.code
        roomId = room.roomId

        playerRepo.saveAll(
            listOf(
                RoomPlayer(roomId = roomId, name = "선수1", displayOrder = 0),
                RoomPlayer(roomId = roomId, name = "선수2", displayOrder = 1),
            ),
        )
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-A", nickname = "팀장A"))
        leaderRepo.save(RoomTeamLeader(roomId = roomId, teamLeaderId = "leader-B", nickname = "팀장B"))
    }

    @Test
    fun `현재 턴의 팀장이 선수를 픽할 수 있다`() {
        val member = draftService.pick(roomCode, "leader-A", "선수1")

        assertThat(member.teamLeaderId).isEqualTo("leader-A")
        assertThat(member.playerName).isEqualTo("선수1")
    }

    @Test
    fun `자신의 턴이 아니면 픽할 수 없다`() {
        assertThatThrownBy { draftService.pick(roomCode, "leader-B", "선수1") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("현재 턴이 아닙니다")
    }

    @Test
    fun `존재하지 않는 선수는 픽할 수 없다`() {
        assertThatThrownBy { draftService.pick(roomCode, "leader-A", "없는선수") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `모든 픽이 완료되면 방이 완료된다`() {
        // Snake: picksPerTeam=1, order=[A, B]
        draftService.pick(roomCode, "leader-A", "선수1")
        draftService.pick(roomCode, "leader-B", "선수2")

        val room = roomRepo.findByCode(roomCode)!!
        assertThat(room.status).isEqualTo(RoomStatus.COMPLETED)
    }
}
