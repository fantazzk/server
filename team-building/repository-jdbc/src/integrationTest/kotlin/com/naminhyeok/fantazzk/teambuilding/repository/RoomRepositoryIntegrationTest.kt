package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.config.TeamBuildingJdbcConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMember
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepositoryAutoConfiguration
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamMemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.test.context.TestConstructor

@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
    TeamBuildingJdbcConfiguration::class,
    RoomRepositoryAutoConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomRepositoryIntegrationTest(
    private val roomRepository: RoomRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
    private val roomBidRepository: RoomBidRepository,
) {
    @Test
    fun `방을 저장하고 코드로 조회할 수 있다`() {
        val saved = createRoom("RM0001")

        assertThat(saved.roomId).isGreaterThan(0)

        val found = roomRepository.findByCode("RM0001")
        assertThat(found).isNotNull
        assertThat(found!!.status).isEqualTo(RoomStatus.WAITING)
        assertThat(found.mode).isEqualTo(TeamBuildingMode.AUCTION)
    }

    @Test
    fun `방 선수를 저장하고 가용 선수를 조회할 수 있다`() {
        val room = createRoom("RM0002")

        roomPlayerRepository.saveAll(
            listOf(
                RoomPlayer(roomId = room.roomId, name = "선수1", displayOrder = 0),
                RoomPlayer(roomId = room.roomId, name = "선수2", displayOrder = 1),
            ),
        )

        val firstAvailable = roomPlayerRepository.findFirstAvailable(room.roomId)
        assertThat(firstAvailable).isNotNull
        assertThat(firstAvailable!!.name).isEqualTo("선수1")
    }

    @Test
    fun `팀장을 저장하고 조회할 수 있다`() {
        val room = createRoom("RM0003")

        roomTeamLeaderRepository.save(
            RoomTeamLeader(roomId = room.roomId, teamLeaderId = "leader-1", nickname = "팀장1", remainingBudget = 300),
        )

        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        assertThat(leaders).hasSize(1)
        assertThat(leaders.first().nickname).isEqualTo("팀장1")
    }

    @Test
    fun `팀 멤버를 저장하고 수를 셀 수 있다`() {
        val room = createRoom("RM0004")

        roomTeamMemberRepository.save(
            RoomTeamMember(roomId = room.roomId, teamLeaderId = "leader-1", playerName = "선수1", assignOrder = 0),
        )

        assertThat(roomTeamMemberRepository.countByRoomId(room.roomId)).isEqualTo(1)
    }

    @Test
    fun `입찰을 저장하고 최고 입찰을 조회할 수 있다`() {
        val room = createRoom("RM0005")

        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-1", amount = 100))
        roomBidRepository.save(RoomBid(roomId = room.roomId, round = 1, teamLeaderId = "leader-2", amount = 150))

        val highest = roomBidRepository.findHighestByRoomIdAndRound(room.roomId, 1)
        assertThat(highest).isNotNull
        assertThat(highest!!.amount).isEqualTo(150)
    }

    private fun createRoom(code: String) =
        roomRepository.save(
            Room(
                code = code,
                hostId = "host",
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            ),
        )
}
